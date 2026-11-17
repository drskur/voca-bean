# APK 자동 업데이트

앱 실행 시 `drskur/voca-bean`의 GitHub 최신 정식 Release를 확인합니다. `update.json`이 없거나 네트워크 연결이 실패하면 앱은 그대로 사용 가능합니다. 더 높은 `versionCode`가 있으면 안내 → 사용자 동의 → 다운로드 → 검증 → Android 설치 화면 순서로 진행합니다. ‘나중에’를 선택하면 다음 앱 실행 시 다시 안내합니다.

Android 보안 정책상 자동 무인 설치는 지원하지 않습니다. 최초 설치 요청 시 ‘이 출처 허용’ 설정이 필요하며, 이후에도 시스템 설치 확인을 거칩니다. 다운로드와 검증은 백그라운드 스레드에서 실행하지만 앱 종료 후 다운로드 재개는 지원하지 않습니다.

## 배포

1. `version.properties`의 `VERSION_CODE`를 증가시키고 `VERSION_NAME`을 설정합니다. Gradle `-PversionCode` / `-PversionName`을 사용했다면 메타데이터도 같은 값으로 생성합니다.
2. **기존 설치본과 동일한 서명 키**로 APK를 빌드합니다. 아래 GitHub Action은 unsigned release APK를 빌드한 뒤 Secrets에 저장한 키로 서명합니다. 수동 배포는 Android Studio 등에서 서명해야 합니다. debug APK와 release APK는 보통 서명이 달라 상호 업데이트할 수 없습니다.
3. 메타데이터를 생성합니다(출력 대상은 새 파일을 사용하세요).

   ```bash
   python scripts/update-manifest.py app-release.apk \
     --version-code 2 --version-name 1.1.0 \
     --notes '업데이트 내용' > update.json
   ```

4. GitHub의 **동일한 정식 Release**에 서명된 APK와 `update.json`을 모두 첨부합니다. Draft/prerelease는 자동 업데이트 대상이 아닙니다. APK 업로드 이름은 `apkFile`과 같아야 합니다.

```json
{
  "versionCode": 2,
  "versionName": "1.1.0",
  "apkFile": "app-release.apk",
  "sha256": "APK 파일의 SHA-256 (64자리 소문자 hex)",
  "notes": "업데이트 내용"
}
```

APK는 HTTPS로 다운로드하며 200 MiB 제한, SHA-256, 패키지 ID, 실제 APK versionCode, 설치본과 동일한 서명 인증서를 검증합니다. 서명 키 교체는 지원하지 않습니다. GitHub API 비인증 요청 제한에 걸리면 해당 실행의 업데이트 확인을 건너뜁니다.

## GitHub Actions 자동 배포

워크플로: `.github/workflows/release.yml`

GitHub 저장소의 **Settings → Secrets and variables → Actions**에 다음 Repository Secrets를 등록합니다.

| Secret | 값 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | 기존 배포 keystore 파일의 Base64 문자열 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 비밀번호 |
| `ANDROID_KEY_ALIAS` | 서명 키 alias |
| `ANDROID_KEY_PASSWORD` | 서명 키 비밀번호 |

Arch Linux에서 `base64 -w 0 /path/to/release.jks`로 Base64 값을 얻을 수 있습니다. 키와 비밀번호는 저장소에 커밋하지 말고 안전하게 백업하세요. 기존 배포 앱이 있다면 반드시 같은 키를 사용해야 합니다.

`version.properties`를 수정·커밋하고 태그를 push하면 배포됩니다.

```bash
# 예: VERSION_CODE=2, VERSION_NAME=1.1.0으로 수정하고 커밋한 뒤
 git tag v1.1.0
 git push origin v1.1.0
```

### mise 태스크

```bash
mise run build          # Debug APK 빌드
mise run test           # 단위 테스트
mise run release:check  # 배포 조건 검사만 수행
mise run release        # 버전 태그 생성 + 브랜치/태그 atomic push
```

`build`와 `test`는 Java가 PATH에 있거나 `JAVA_HOME`이 설정되어 있어야 합니다. 이 환경에서는 `JAVA_HOME=/opt/android-studio/jbr mise run build`처럼 실행할 수 있습니다.

배포 전 `version.properties`를 수정하고 모든 변경 사항을 커밋하세요. `release`는 현재 브랜치와 `v${VERSION_NAME}` 태그를 origin에 함께 push합니다. **실행 즉시 GitHub 배포가 시작됩니다.** dirty 작업 트리, detached HEAD, 기존 태그는 거부하며 강제 push는 하지 않습니다. push 실패 시 로컬 태그는 보존하고 재시도 명령을 안내합니다. `release:check`도 원격 태그 검사를 위해 네트워크 연결이 필요합니다.

태그는 반드시 `v${VERSION_NAME}`이어야 하며 자동 배포는 `1.1.0` 형태의 정식 버전만 지원합니다. 매 배포마다 `VERSION_CODE`를 증가시키세요. 워크플로에서 기존 릴리스와의 versionCode 비교는 수행하지 않습니다.

Action은 JDK 25와 Android SDK 37을 준비하고, 단위 테스트 → release 빌드 → zipalign/서명/서명 검증 → `update.json` 생성 → Draft Release에 두 파일 업로드 → 정식 최신 Release 공개 순서로 진행합니다. `GITHUB_TOKEN`의 `contents: write` 권한으로 업로드하므로 별도 PAT는 필요하지 않습니다.

기존 Release/첨부 파일은 덮어쓰지 않습니다. 실패 후 Draft Release가 남았다면 상태를 확인하고 GitHub에서 직접 정리한 다음 실패한 워크플로를 재실행하세요. 공개된 릴리스는 덮어쓰지 말고 새 버전으로 배포하는 것이 안전합니다.

## 확인

```bash
JAVA_HOME=/opt/android-studio/jbr ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

실기기에서는 낮은 버전 설치 후 높은 버전의 동일 서명 Release로 확인하세요. 오프라인 실행, 업데이트 거절, 출처 허용 거절/허용, 잘못된 해시, 다른 서명, 설치 취소도 확인해야 합니다.

`REQUEST_INSTALL_PACKAGES` 권한은 APK 직접 배포를 위한 것입니다. Google Play 배포로 전환한다면 해당 권한을 제거하고 Play In-App Updates로 교체해야 합니다.
