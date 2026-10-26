param([Parameter(Mandatory=$true)][string]$SessionDirectory)
$ErrorActionPreference='Stop'
$taskSession=[IO.Path]::GetFullPath($SessionDirectory)
if(Test-Path -LiteralPath (Join-Path $taskSession 'ready')){throw 'Use a fresh dedicated session directory'}
$taskChildren=[Collections.Generic.List[object]]::new()
function Start-Role([string]$Role,[string]$PlanRole) {
    $plan=Get-Content -LiteralPath (Join-Path $taskSession "launch-$PlanRole.json") -Raw|ConvertFrom-Json
    New-Item -ItemType Directory -Path $plan.workingDirectory -Force | Out-Null
    $info=[Diagnostics.ProcessStartInfo]::new(); $info.FileName=$plan.executable; $info.WorkingDirectory=$plan.workingDirectory
    $info.UseShellExecute=$false; $info.CreateNoWindow=$true; $info.RedirectStandardOutput=$true; $info.RedirectStandardError=$true; $info.RedirectStandardInput=$true
    foreach($argument in $plan.arguments){$info.ArgumentList.Add([string]$argument)}
    foreach($entry in $plan.environment.PSObject.Properties){$info.Environment[$entry.Name]=[string]$entry.Value}
    $process=[Diagnostics.Process]::new(); $process.StartInfo=$info
    if(!$process.Start()){throw "Cannot start $Role"}
    $record=[pscustomobject]@{Role=$Role;Process=$process;OutStream=$null;ErrStream=$null;OutCopy=$null;ErrCopy=$null}
    $taskChildren.Add($record)
    $record.OutStream=[IO.FileStream]::new((Join-Path $taskSession "$Role-stdout.log"),[IO.FileMode]::Create,[IO.FileAccess]::Write,[IO.FileShare]::ReadWrite,1,$true)
    $record.ErrStream=[IO.FileStream]::new((Join-Path $taskSession "$Role-stderr.log"),[IO.FileMode]::Create,[IO.FileAccess]::Write,[IO.FileShare]::ReadWrite,1,$true)
    $record.OutCopy=$process.StandardOutput.BaseStream.CopyToAsync($record.OutStream); $record.ErrCopy=$process.StandardError.BaseStream.CopyToAsync($record.ErrStream)
    Write-Host "Started dedicated test $Role PID $($process.Id)"
    return $record
}
function Wait-Roles([DateTime]$Deadline) {
    while(@($taskChildren|Where-Object{!$_.Process.HasExited}).Count-gt0){
        foreach($child in $taskChildren){
            if(Test-Path -LiteralPath (Join-Path $taskSession "$($child.Role)-failed")){throw "Assertion failed for $($child.Role)"}
            if($child.Process.HasExited-and$child.Process.ExitCode-ne0){throw "Nonzero exit for $($child.Role): $($child.Process.ExitCode)"}
        }
        if(Test-Path -LiteralPath (Join-Path $taskSession 'server-failed')){throw 'Dedicated server assertion failed'}
        if([DateTime]::UtcNow-gt$Deadline){throw 'Dedicated scenario timeout'}
        Start-Sleep -Milliseconds 300
    }
}
try {
    $server=Start-Role 'server' 'server'; $readyDeadline=[DateTime]::UtcNow.AddSeconds(150)
    while(!(Test-Path -LiteralPath (Join-Path $taskSession 'ready'))){
        if($server.Process.HasExited){throw 'Dedicated server exited before ready'}
        if([DateTime]::UtcNow-gt$readyDeadline){throw 'Dedicated server startup timeout'}
        Start-Sleep -Milliseconds 200
    }
    $owner=Start-Role 'owner' 'owner'; $guest=Start-Role 'guest' 'guest'
    Wait-Roles ([DateTime]::UtcNow.AddMinutes(6))
    $first=Get-Content -LiteralPath (Join-Path $taskSession 'phase1-report.json') -Raw|ConvertFrom-Json
    if($first.status-ne'passed'){throw 'Dedicated network phase did not pass'}
    $reopen=Start-Role 'reopen' 'server'
    Wait-Roles ([DateTime]::UtcNow.AddMinutes(3))
    $report=Get-Content -LiteralPath (Join-Path $taskSession 'report.json') -Raw|ConvertFrom-Json
    if($report.status-ne'passed'-or!$report.actualWorldRestart){throw 'Actual world restart did not pass'}
    $exits=[ordered]@{}
    foreach($child in $taskChildren){
        $child.Process.WaitForExit(); [void]$child.OutCopy.GetAwaiter().GetResult(); [void]$child.ErrCopy.GetAwaiter().GetResult()
        if($child.Process.ExitCode-ne0){throw "Final nonzero exit for $($child.Role): $($child.Process.ExitCode)"}
        $exits[$child.Role]=$child.Process.ExitCode
    }
    [IO.File]::WriteAllText((Join-Path $taskSession 'process-exits.json'),($exits|ConvertTo-Json))
    Write-Host 'Ordinary DedicatedServer, two independent clients and saved-world restart passed.'
} finally {
    foreach($child in $taskChildren){
        if(!$child.Process.HasExited){
            if($child.Role-in@('server','reopen')){$child.Process.StandardInput.WriteLine('stop');$child.Process.StandardInput.Flush()}
            else{[void]$child.Process.CloseMainWindow()}
            if(!$child.Process.WaitForExit(15000)){$child.Process.Kill($true);$child.Process.WaitForExit()}
        }
        if($child.OutCopy){[void]$child.OutCopy.GetAwaiter().GetResult()}; if($child.ErrCopy){[void]$child.ErrCopy.GetAwaiter().GetResult()}
        if($child.OutStream){$child.OutStream.Dispose()}; if($child.ErrStream){$child.ErrStream.Dispose()}; $child.Process.Dispose()
    }
}
