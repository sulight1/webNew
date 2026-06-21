# Run PowerShell as Administrator, then:
#   cd D:\webNew\finger-art-weixin\scripts
#   .\open-firewall-3000.ps1

$ruleName = "Finger Art Backend 3000"

netsh advfirewall firewall show rule name="$ruleName" | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Rule already exists: $ruleName"
}
else {
    netsh advfirewall firewall add rule name="$ruleName" dir=in action=allow protocol=TCP localport=3000
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed. Please run as Administrator."
        exit 1
    }
    Write-Host "Firewall rule added for TCP port 3000."
}

Write-Host ""
Write-Host "Test on phone browser (same WiFi):"
Write-Host "http://192.168.40.46:3000/products?scope=approved"
