Set-Variable -Name "path" -Value "./assets/scripts/compiler/typescript.js"

if(![System.IO.File]::Exists($path)){
    Invoke-WebRequest -O ./assets/scripts/compiler/typescript.js https://unpkg.com/typescript@latest/lib/typescript.js
}