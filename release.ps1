Set-Location "C:\Users\Sandun Madhushan\AndroidStudioProjects\BookDiary"

# Step 1 - stage and commit
git add -A
$commitResult = git commit -m "chore: add shareable APK and update README for v1.0.0" 2>&1
Write-Host "Commit: $commitResult"

# Step 2 - push
$pushResult = git push origin main 2>&1
Write-Host "Push: $pushResult"

# Step 3 - create tag
$tagResult = git tag v1.0.0 2>&1
Write-Host "Tag: $tagResult"

$tagPushResult = git push origin v1.0.0 2>&1
Write-Host "Tag push: $tagPushResult"

# Step 4 - create GitHub release
$ghPath = "C:\Program Files\GitHub CLI\gh.exe"
$apkPath = "shareable_apk\BookDiary.apk"
$notesPath = "release_notes.md"

$releaseResult = & $ghPath release create v1.0.0 $apkPath --repo "nethmaHarini/BookDiary" --title "BookDiary v1.0.0" --notes-file $notesPath 2>&1
Write-Host "Release: $releaseResult"


