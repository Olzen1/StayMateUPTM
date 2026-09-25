# Fix Firestore PERMISSION_DENIED and Fatal Crash

This plan addresses the `PERMISSION_DENIED` error by providing the necessary Firestore Security Rules and improves the app's stability by adding error handling to critical Firestore calls.

## User Review Required

> [!IMPORTANT]
> This fix requires you to update your **Firestore Security Rules** in the Firebase Console. The app code changes will prevent the crash and show error messages, but the app won't be able to load data until the rules are updated.

## Proposed Changes

### Firestore Configuration

#### [NEW] [firestore.rules](file:///C:/UPTM/StayMateUPTM/firestore.rules)
I will provide a standard set of rules that allow authenticated users to read and write to the necessary collections. You should copy these to your Firebase Console under **Firestore Database > Rules**.

### App Logic Improvements

#### [MODIFY] [AddPostViewModel.kt](file:///C:/UPTM/StayMateUPTM/app/src/main/java/com/staymate/uptm/viewmodel/AddPostViewModel.kt)
- Wrap the profile fetch in a `try-catch` block to prevent crashes.
- Fix the `first()` import to ensure we use the coroutines version.
- Update the state to `Error` if the profile fetch fails.

#### [MODIFY] [PostRepository.kt](file:///C:/UPTM/StayMateUPTM/app/src/main/java/com/staymate/uptm/repository/PostRepository.kt)
- Add a safety check to ensure `Post` objects are correctly parsed, which can sometimes trigger permission issues if the schema doesn't match rules (though less common).

## Verification Plan

### Manual Verification
1.  **Rules Update:** Copy the provided `firestore.rules` to the Firebase Console.
2.  **App Run:** Deploy the app to a device.
3.  **Feed Check:** Verify that the "No posts yet" or "Error" message is shown instead of a crash.
4.  **Onboarding:** Verify that the onboarding process completes successfully now that the `users` collection is accessible.
5.  **Post Creation:** Try creating a post; it should now successfully fetch the profile and save the post.
