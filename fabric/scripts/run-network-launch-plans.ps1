param([Parameter(Mandatory=$true)][string]$SessionDirectory)
$ErrorActionPreference='Stop'
$sessionPath=[IO.Path]::GetFullPath($SessionDirectory)
if(Test-Path -LiteralPath (Join-Path $sessionPath 'ready')){throw 'Use a fresh network test session directory'}
$children=[Collections.Generic.List[object]]::new()
function Start-TestRole([string]$Role) {
    $plan=Get-Content -LiteralPath (Join-Path $sessionPath "launch-$Role.json") -Raw|ConvertFrom-Json
    New-Item -ItemType Directory -Path $plan.workingDirectory -Force|Out-Null
    $info=[Diagnostics.ProcessStartInfo]::new()
    $info.FileName=$plan.executable
    $info.WorkingDirectory=$plan.workingDirectory
    $info.UseShellExecute=$false
    $info.CreateNoWindow=$true
    $info.RedirectStandardOutput=$true
    $info.RedirectStandardError=$true
    foreach($argument in $plan.arguments){$info.ArgumentList.Add([string]$argument)}
    foreach($entry in $plan.environment.PSObject.Properties){$info.Environment[$entry.Name]=[string]$entry.Value}
    $process=[Diagnostics.Process]::new();$process.StartInfo=$info
    if(!$process.Start()){throw "Cannot start test $Role"}
    $record=[pscustomobject]@{Role=$Role;Process=$process;OutStream=$null;ErrStream=$null;OutCopy=$null;ErrCopy=$null}
    $children.Add($record)
    $record.OutStream=[IO.FileStream]::new((Join-Path $sessionPath "$Role-stdout.log"),[IO.FileMode]::Create,[IO.FileAccess]::Write,[IO.FileShare]::ReadWrite,1,$true)
    $record.ErrStream=[IO.FileStream]::new((Join-Path $sessionPath "$Role-stderr.log"),[IO.FileMode]::Create,[IO.FileAccess]::Write,[IO.FileShare]::ReadWrite,1,$true)
    $record.OutCopy=$process.StandardOutput.BaseStream.CopyToAsync($record.OutStream)
    $record.ErrCopy=$process.StandardError.BaseStream.CopyToAsync($record.ErrStream)
    Write-Host "Started network test $Role, PID $($process.Id)"
    return $record
}
try {
    $server=Start-TestRole 'server'
    $readyDeadline=[DateTime]::UtcNow.AddSeconds(100)
    while(!(Test-Path -LiteralPath (Join-Path $sessionPath 'ready'))) {
        if($server.Process.HasExited){throw "Network server exited during startup: $($server.Process.ExitCode)"}
        if([DateTime]::UtcNow-gt$readyDeadline){throw 'Network server startup timeout'}
        Start-Sleep -Milliseconds 200
    }
    $owner=Start-TestRole 'owner'
    $guest=Start-TestRole 'guest'
    $deadline=[DateTime]::UtcNow.AddMinutes(6)
    while(@($children|Where-Object{!$_.Process.HasExited}).Count-gt0) {
        foreach($child in $children){if(Test-Path -LiteralPath (Join-Path $sessionPath "$($child.Role)-failed")){throw "Network $($child.Role) assertion failed; see its log"};if($child.Process.HasExited-and$child.Process.ExitCode-ne0){throw "Network $($child.Role) failed: exit$($child.Process.ExitCode)"}}
        if([DateTime]::UtcNow-gt$deadline){throw 'Network test timeout'}
        Start-Sleep -Milliseconds 300
    }
    foreach($child in $children){$child.Process.WaitForExit();[void]$child.OutCopy.GetAwaiter().GetResult();[void]$child.ErrCopy.GetAwaiter().GetResult();if($child.Process.ExitCode-ne0){throw "Network $($child.Role) failed at final exit: $($child.Process.ExitCode)"}}
    $report=Get-Content -LiteralPath (Join-Path $sessionPath 'report.json') -Raw|ConvertFrom-Json
    if($report.status-ne'passed'){throw 'Network assertions did not complete'}
    $exits=[ordered]@{}
    foreach($child in $children){$exits[$child.Role]=$child.Process.ExitCode}
    [IO.File]::WriteAllText((Join-Path $sessionPath 'process-exits.json'),($exits|ConvertTo-Json))
    Write-Host 'Two independent clients and standalone GameTestServer passed all network assertions.'
} finally {
    foreach($child in $children) {
        if(!$child.Process.HasExited){$child.Process.Kill($true);$child.Process.WaitForExit()}
        if($child.OutCopy){[void]$child.OutCopy.GetAwaiter().GetResult()}
        if($child.ErrCopy){[void]$child.ErrCopy.GetAwaiter().GetResult()}
        if($child.OutStream){$child.OutStream.Dispose()}
        if($child.ErrStream){$child.ErrStream.Dispose()}
        $child.Process.Dispose()
    }
}
