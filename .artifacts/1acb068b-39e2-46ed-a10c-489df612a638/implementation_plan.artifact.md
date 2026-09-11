# Fix Red Errors in activity_login.xml

The layout file is showing errors because the project is missing the necessary dependencies for `ConstraintLayout` and `Material Components`. Additionally, there is an invalid resource reference and the app theme needs to be updated to support Material components.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/UPTM/StayMateUPTM/gradle/libs.versions.toml)
- Add version definitions for `material` and `constraintlayout`.
- Add library definitions for `material` and `constraintlayout`.

#### [MODIFY] [build.gradle.kts](file:///C:/UPTM/StayMateUPTM/app/build.gradle.kts)
- Add `libs.material` and `libs.androidx.constraintlayout` to the dependencies block.

### Resources & UI

#### [MODIFY] [activity_login.xml](file:///C:/UPTM/StayMateUPTM/app/src/main/res/layout/activity_login.xml)
- Fix invalid resource reference `@android:drawable/ic_lock_lock` to `@drawable/ic_lock`.
- (Optional) Create a new `ic_lock.xml` drawable.

#### [NEW] [ic_lock.xml](file:///C:/UPTM/StayMateUPTM/app/src/main/res/drawable/ic_lock.xml)
- Create a simple vector icon for the password field.

#### [MODIFY] [themes.xml](file:///C:/UPTM/StayMateUPTM/app/src/main/res/values/themes.xml)
- Update parent theme to `Theme.Material3.DayNight.NoActionBar` to ensure Material components (Buttons, TextFields) render correctly.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to verify the build passes.

### Manual Verification
- The user should see the errors disappear in the `activity_login.xml` editor after a Gradle Sync.
