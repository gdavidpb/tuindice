# Common UI Coverage Matrix

_Generated automatically on 2026-07-20 01:50:32 -0400_

## Summary by module

| Module | when_ threshold | commonMain composable files | @Composable occurrences | commonTest UI tests | Nominal tests | UiTest files meeting threshold |
|---|---:|---:|---:|---:|---:|---:|
| `about` | 2 | 8 | 8 | 7 | 7 | 7 |
| `auth` | 2 | 20 | 20 | 18 | 16 | 16 |
| `base` | 2 | 32 | 44 | 18 | 18 | 18 |
| `enrollmentproof` | 2 | 5 | 5 | 5 | 5 | 5 |
| `evaluations` | 2 | 42 | 48 | 31 | 24 | 24 |
| `maincore` | 3 | 14 | 17 | 10 | 9 | 9 |
| `pensum` | 2 | 40 | 47 | 2 | 2 | 2 |
| `record` | 2 | 50 | 62 | 11 | 7 | 7 |
| `subjects` | 2 | 24 | 27 | 4 | 3 | 3 |
| `summary` | 2 | 17 | 20 | 14 | 10 | 10 |
| `wizard` | 2 | 2 | 3 | 1 | 1 | 1 |

## Detail by composable file

### `about`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/presentation/route/AboutRoute.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/presentation/route/AboutRouteUiTest.kt` | 18 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/screen/AboutScreen.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/screen/AboutScreenUiTest.kt` | 3 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutContentView.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutContentViewUiTest.kt` | 3 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutHeader.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutHeaderUiTest.kt` | 2 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutIdleView.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutIdleViewUiTest.kt` | 2 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutItem.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutItemUiTest.kt` | 2 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutSpanText.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutSpanTextUiTest.kt` | 2 | PASS threshold (2) |
| `about/src/commonMain/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutSwitchItem.kt` | 1 | `about/src/commonTest/kotlin/com/gdavidpb/tuindice/about/ui/view/AboutSwitchItemUiTest.kt` | 0 | MISSING TEST |

### `auth`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/presentation/route/SignInRoute.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/presentation/route/SignInRouteUiTest.kt` | 11 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/presentation/route/SignOutRoute.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/presentation/route/SignOutRouteUiTest.kt` | 3 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/presentation/route/UpdatePasswordRoute.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/presentation/route/UpdatePasswordRouteUiTest.kt` | 4 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/CancelButton.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/CancelButtonUiTest.kt` | 0 | MISSING TEST |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/ConfirmButton.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/ConfirmButtonUiTest.kt` | 0 | MISSING TEST |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SecondaryButton.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SecondaryButtonUiTest.kt` | 0 | MISSING TEST |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutContentDialog.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutContentDialogUiTest.kt` | 2 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutDialog.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutDialogUiTest.kt` | 3 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutDialogActions.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/SignOutDialogActionsUiTest.kt` | 0 | MISSING TEST |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/UpdatePasswordContentDialog.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/UpdatePasswordContentDialogUiTest.kt` | 3 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/UpdatePasswordDialog.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/dialog/UpdatePasswordDialogUiTest.kt` | 3 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/screen/SignInScreen.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/screen/SignInScreenUiTest.kt` | 4 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/AnimatedPatternBackground.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/AnimatedPatternBackgroundUiTest.kt` | 2 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/LinkText.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/LinkTextUiTest.kt` | 2 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/PasswordTextField.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/PasswordTextFieldUiTest.kt` | 4 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/RandomFlipperText.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/RandomFlipperTextUiTest.kt` | 2 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/SignInIdleView.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/SignInIdleViewUiTest.kt` | 4 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/SignInLoggingInView.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/SignInLoggingInViewUiTest.kt` | 2 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/UpdatePasswordIdleView.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/UpdatePasswordIdleViewUiTest.kt` | 3 | PASS threshold (2) |
| `auth/src/commonMain/kotlin/com/gdavidpb/tuindice/auth/ui/view/UsbIdTextField.kt` | 1 | `auth/src/commonTest/kotlin/com/gdavidpb/tuindice/auth/ui/view/UsbIdTextFieldUiTest.kt` | 7 | PASS threshold (2) |

### `base`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/presentation/model/UiTextString.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/presentation/model/UiTextStringUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ConfirmationDialog.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ConfirmationDialogUiTest.kt` | 6 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ConfirmationDialogEntry.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ConfirmationDialogEntryUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ExternalResourceDialog.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/dialog/ExternalResourceDialogUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/style/AcademicStatusColors.kt` | 8 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/style/AcademicStatusColorsUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/style/TuIndiceDarkTheme.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/style/TuIndiceDarkThemeUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/style/TuIndiceShellColors.kt` | 4 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/style/TuIndiceShellColorsUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/AppLogoView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/AppLogoViewUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/DropdownMenuTextField.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/DropdownMenuTextFieldUiTest.kt` | 3 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/EmptyStateAnimationView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/EmptyStateAnimationViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/EmptyView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/EmptyViewUiTest.kt` | 3 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/ErrorStateAnimationView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/ErrorStateAnimationViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/ErrorView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/ErrorViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/IllustratedMessageView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/IllustratedMessageViewUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/LoadingView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/LoadingViewUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/LottieResourceAnimationView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/LottieResourceAnimationViewUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/OutdatedAppAnimationView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/OutdatedAppAnimationViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/OutdatedAppScreen.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/OutdatedAppScreenUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/PeekingSelectorView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/PeekingSelectorViewUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/PulsingIconHalo.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/PulsingIconHaloUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/SealedCrossfade.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/SealedCrossfadeUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/SearchTextField.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/SearchTextFieldUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/StatsLoadingAnimationView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/StatsLoadingAnimationViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/SubjectCodeChip.kt` | 2 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/SubjectCodeChipUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/SubjectResultCard.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/SubjectResultCardUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/TopAppBarActionsView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/TopAppBarActionsViewUiTest.kt` | 4 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/TopAppBarAnimatedTitleView.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/TopAppBarAnimatedTitleViewUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/view/WheelPicker.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/ui/view/WheelPickerUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/extension/CollectNavResultWithLifecycle.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/utils/extension/CollectNavResultWithLifecycleUiTest.kt` | 0 | MISSING TEST |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/extension/Compose.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/utils/extension/ComposeUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/extension/Flow.kt` | 1 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/utils/extension/FlowUiTest.kt` | 2 | PASS threshold (2) |
| `base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/extension/Navigation.kt` | 2 | `base/src/commonTest/kotlin/com/gdavidpb/tuindice/base/utils/extension/NavigationUiTest.kt` | 2 | PASS threshold (2) |

### `enrollmentproof`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `enrollmentproof/src/commonMain/kotlin/com/gdavidpb/tuindice/enrollmentproof/presentation/route/EnrollmentProofRoute.kt` | 1 | `enrollmentproof/src/commonTest/kotlin/com/gdavidpb/tuindice/enrollmentproof/presentation/route/EnrollmentProofRouteUiTest.kt` | 11 | PASS threshold (2) |
| `enrollmentproof/src/commonMain/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/dialog/EnrollmentProofContentDialog.kt` | 1 | `enrollmentproof/src/commonTest/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/dialog/EnrollmentProofContentDialogUiTest.kt` | 2 | PASS threshold (2) |
| `enrollmentproof/src/commonMain/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/dialog/EnrollmentProofFetchingSheet.kt` | 1 | `enrollmentproof/src/commonTest/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/dialog/EnrollmentProofFetchingSheetUiTest.kt` | 3 | PASS threshold (2) |
| `enrollmentproof/src/commonMain/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/view/EnrollmentProofFetchingView.kt` | 1 | `enrollmentproof/src/commonTest/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/view/EnrollmentProofFetchingViewUiTest.kt` | 3 | PASS threshold (2) |
| `enrollmentproof/src/commonMain/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/view/EnrollmentProofLoadingView.kt` | 1 | `enrollmentproof/src/commonTest/kotlin/com/gdavidpb/tuindice/enrollmentproof/ui/view/EnrollmentProofLoadingViewUiTest.kt` | 2 | PASS threshold (2) |

### `evaluations`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/EvaluationDateTextMapping.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/EvaluationDateTextMappingUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/EvaluationItemMapping.kt` | 2 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/EvaluationItemMappingUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/TypePicker.kt` | 2 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/presentation/mapper/TypePickerUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/route/EvaluationRoute.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/presentation/route/EvaluationRouteUiTest.kt` | 9 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/presentation/route/EvaluationsRoute.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/presentation/route/EvaluationsRouteUiTest.kt` | 9 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/DeleteEvaluationConfirmationContentDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/DeleteEvaluationConfirmationContentDialogUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/DiscardEvaluationContentDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/DiscardEvaluationContentDialogUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/EvaluationGradePickerContentDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/EvaluationGradePickerContentDialogUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/GradePickerContentDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/GradePickerContentDialogUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/GradePickerDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/GradePickerDialogUiTest.kt` | 9 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/MaxGradePickerContentDialog.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/dialog/MaxGradePickerContentDialogUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/screen/EvaluationScreen.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/screen/EvaluationScreenUiTest.kt` | 3 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/screen/EvaluationsScreen.kt` | 4 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/screen/EvaluationsScreenUiTest.kt` | 6 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/CalendarDayCell.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/CalendarDayCellUiTest.kt` | 3 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActionButton.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActionButtonUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActions.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActionsUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActionsContainer.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationActionsContainerUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationAttemptPicker.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationAttemptPickerUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationCalendarContent.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationCalendarContentUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationContentView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationContentViewUiTest.kt` | 7 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationDatePicker.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationDatePickerUiTest.kt` | 5 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationFailedView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationFailedViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationGradeActionButton.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationGradeActionButtonUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationGradeWheelPicker.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationGradeWheelPickerUiTest.kt` | 3 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationHeaderView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationHeaderViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationItemView.kt` | 2 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationItemViewUiTest.kt` | 4 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationLoadingView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationLoadingViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationMetadataRow.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationMetadataRowUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationRequiredFieldError.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationRequiredFieldErrorUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationSwipeToDismiss.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationSwipeToDismissUiTest.kt` | 5 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationTypePicker.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationTypePickerUiTest.kt` | 3 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekDayView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekDayViewUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekHeaderView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekHeaderViewUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekSelectorItemView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationWeekSelectorItemViewUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsContentView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsContentViewUiTest.kt` | 3 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsEmptyMatchView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsEmptyMatchViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsEmptyView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsEmptyViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsFailedView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsFailedViewUiTest.kt` | 2 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsNoAttemptsView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsNoAttemptsViewUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsViewUiTest.kt` | 4 | PASS threshold (2) |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsWeekStripView.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/EvaluationsWeekStripViewUiTest.kt` | 0 | MISSING TEST |
| `evaluations/src/commonMain/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/WeekdayHeaderRow.kt` | 1 | `evaluations/src/commonTest/kotlin/com/gdavidpb/tuindice/evaluations/ui/view/WeekdayHeaderRowUiTest.kt` | 2 | PASS threshold (2) |

### `maincore`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/presentation/navigation/RememberTuIndiceNavigator.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/presentation/navigation/RememberTuIndiceNavigatorUiTest.kt` | 0 | MISSING TEST |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/presentation/route/BrowserRoute.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/presentation/route/BrowserRouteUiTest.kt` | 6 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/presentation/route/MainRoute.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/presentation/route/MainRouteUiTest.kt` | 6 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/presentation/route/TuIndiceAppHostRoute.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/presentation/route/TuIndiceAppHostRouteUiTest.kt` | 11 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/dialog/GooglePlayServicesDialog.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/dialog/GooglePlayServicesDialogUiTest.kt` | 3 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceEntryScopeDecorator.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceEntryScopeDecoratorUiTest.kt` | 0 | MISSING TEST |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceRetainedSavedStateDecorator.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceRetainedSavedStateDecoratorUiTest.kt` | 0 | MISSING TEST |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceRetainedViewModelStoreDecorator.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/navigation/TuIndiceRetainedViewModelStoreDecoratorUiTest.kt` | 0 | MISSING TEST |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/AppAvailabilityNoticeScreen.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/screen/AppAvailabilityNoticeScreenUiTest.kt` | 0 | MISSING TEST |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/BrowserScreen.kt` | 2 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/screen/BrowserScreenUiTest.kt` | 7 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceNavDisplay.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceNavDisplayUiTest.kt` | 3 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceScreen.kt` | 3 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceScreenUiTest.kt` | 14 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/theme/TuIndiceSharedTheme.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/theme/TuIndiceSharedThemeUiTest.kt` | 4 | PASS threshold (3) |
| `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/view/TopBarBannerHost.kt` | 1 | `maincore/src/commonTest/kotlin/com/gdavidpb/tuindice/ui/view/TopBarBannerHostUiTest.kt` | 3 | PASS threshold (3) |

### `pensum`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/presentation/route/PensumRoute.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/presentation/route/PensumRouteUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumCurrentSelectionSummary.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumCurrentSelectionSummaryUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumFulfilledSubjectSummary.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumFulfilledSubjectSummaryUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumOpenRelatedSubjectIcon.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumOpenRelatedSubjectIconUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumRouteConnector.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumRouteConnectorUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumRouteRelationCard.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumRouteRelationCardUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSelectedSubjectRouteCard.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSelectedSubjectRouteCardUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSelectionBottomSheet.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSelectionBottomSheetUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumStyledSubjectCodeChip.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumStyledSubjectCodeChipUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectCorequisiteSection.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectCorequisiteSectionUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectDetailBottomSheet.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectDetailBottomSheetUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectDetailMeta.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectDetailMetaUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectExpandedDetailContent.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectExpandedDetailContentUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectMoreDetailButton.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectMoreDetailButtonUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectOverviewCard.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectOverviewCardUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationRow.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationRowUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationStatusBadge.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationStatusBadgeUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationStatusMarker.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRelationStatusMarkerUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRouteColumn.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRouteColumnUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRouteContext.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectRouteContextUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectStatusBadge.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/dialog/PensumSubjectStatusBadgeUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/screen/PensumScreen.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/screen/PensumScreenUiTest.kt` | 38 | PASS threshold (2) |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumCanvasLegend.kt` | 5 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumCanvasLegendUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumContentView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumContentViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumEmptyView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumEmptyViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumGraphCanvas.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumGraphCanvasUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumGraphTokens.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumGraphTokensUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLoadingAnimationView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLoadingAnimationViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLoadingView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLoadingViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLocalDataWarningView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumLocalDataWarningViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumMinimap.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumMinimapUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumModalityOptionRow.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumModalityOptionRowUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumNodeCard.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumNodeCardUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumProgressRing.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumProgressRingUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumRefreshingIndicatorView.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumRefreshingIndicatorViewUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumStatusIconMarker.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumStatusIconMarkerUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumStickyTermHeader.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumStickyTermHeaderUiTest.kt` | 5 | PASS threshold (2) |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumSubjectCodeChip.kt` | 1 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumSubjectCodeChipUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumSummaryRow.kt` | 3 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumSummaryRowUiTest.kt` | 0 | MISSING TEST |
| `pensum/src/commonMain/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumZoomControls.kt` | 2 | `pensum/src/commonTest/kotlin/com/gdavidpb/tuindice/pensum/ui/view/PensumZoomControlsUiTest.kt` | 0 | MISSING TEST |

### `record`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/mapper/AttemptItem.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/presentation/mapper/AttemptItemUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/mapper/TermItem.kt` | 3 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/presentation/mapper/TermItemUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/route/CreateSyntheticTermRoute.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/presentation/route/CreateSyntheticTermRouteUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/route/RecordRoute.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/presentation/route/RecordRouteUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DeleteSyntheticTermConfirmationContentDialog.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DeleteSyntheticTermConfirmationContentDialogUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DeleteSyntheticTermConfirmationDialog.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DeleteSyntheticTermConfirmationDialogUiTest.kt` | 2 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DiscardSyntheticTermContentDialog.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/DiscardSyntheticTermContentDialogUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionBottomSheet.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionBottomSheetUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionTermRowView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionTermRowViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionYearHeaderView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/dialog/TermSelectionYearHeaderViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/screen/CreateSyntheticTermScreen.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/screen/CreateSyntheticTermScreenUiTest.kt` | 14 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/screen/RecordScreen.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/screen/RecordScreenUiTest.kt` | 6 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/ApprovedSearchResultsToggleView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/ApprovedSearchResultsToggleViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptCardItemView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptCardItemViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptItemView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptItemViewUiTest.kt` | 4 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptStatusBadge.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptStatusBadgeUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptStatusChip.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/AttemptStatusChipUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermAddSubjectTabsView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermAddSubjectTabsViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermControlLabel.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermControlLabelUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermLoadChipView.kt` | 6 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermLoadChipViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermPeriodRowView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermPeriodRowViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchFieldView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchFieldViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchGuidanceView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchGuidanceViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchMessageView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSearchMessageViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSectionTitleView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSectionTitleViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSelectedSubjectCardView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSelectedSubjectCardViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectActionButtonView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectActionButtonViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectCodeChipView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectCodeChipViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectStatsButtonView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectStatsButtonViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectStatusRowView.kt` | 4 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubjectStatusRowViewUiTest.kt` | 5 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubmitBarView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSubmitBarViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSuggestedSubjectCardView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/CreateTermSuggestedSubjectCardViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/QualitativeStatusSelector.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/QualitativeStatusSelectorUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordContentView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordContentViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordEmptyView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordEmptyViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordEnrollmentProofActionView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordEnrollmentProofActionViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordFailedView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordFailedViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordSyntheticTermActionsView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordSyntheticTermActionsViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTermPagerView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTermPagerViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTopBarViewModeBannerView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTopBarViewModeBannerViewUiTest.kt` | 2 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTopBarViewModeSwitchView.kt` | 3 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordTopBarViewModeSwitchViewUiTest.kt` | 2 | PASS threshold (2) |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordViewModeBannerColorsProvider.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/RecordViewModeBannerColorsProviderUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/SelectedTermView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/SelectedTermViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermDeltaChip.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermDeltaChipUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermItemView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermItemViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermMetricDivider.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermMetricDividerUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermMetricItem.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermMetricItemUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSelectorView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSelectorViewUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSummaryContent.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSummaryContentUiTest.kt` | 0 | MISSING TEST |
| `record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSummaryView.kt` | 1 | `record/src/commonTest/kotlin/com/gdavidpb/tuindice/record/ui/view/TermSummaryViewUiTest.kt` | 0 | MISSING TEST |

### `subjects`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/presentation/route/SubjectDetailRoute.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/presentation/route/SubjectDetailRouteUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/presentation/route/SubjectSearchRoute.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/presentation/route/SubjectSearchRouteUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/screen/SubjectDetailScreen.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/screen/SubjectDetailScreenUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/screen/SubjectSearchScreen.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/screen/SubjectSearchScreenUiTest.kt` | 5 | PASS threshold (2) |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailBarChartCard.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailBarChartCardUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailBarChartColumnProvider.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailBarChartColumnProviderUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailChartsView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailChartsViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailContentView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailContentViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailHeaderView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailHeaderViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailHighlightKpiCard.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailHighlightKpiCardUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailKpiRowView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailKpiRowViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailLoadingView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailLoadingViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailMessageView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailMessageViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailNumericGradeChartCard.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailNumericGradeChartCardUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSegmentSummaryView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSegmentSummaryViewUiTest.kt` | 3 | PASS threshold (2) |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSegmentTabsView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSegmentTabsViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailStandardKpiCard.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailStandardKpiCardUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSummaryMetric.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectDetailSummaryMetricUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchError.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchErrorUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchGuidanceView.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchGuidanceViewUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchMessage.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchMessageUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchResultCard.kt` | 4 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchResultCardUiTest.kt` | 3 | PASS threshold (2) |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchResults.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchResultsUiTest.kt` | 0 | MISSING TEST |
| `subjects/src/commonMain/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchTextField.kt` | 1 | `subjects/src/commonTest/kotlin/com/gdavidpb/tuindice/subjects/ui/view/SubjectSearchTextFieldUiTest.kt` | 0 | MISSING TEST |

### `summary`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/presentation/route/SummaryRoute.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/presentation/route/SummaryRouteUiTest.kt` | 13 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/ProfilePictureSettingsContentDialog.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/ProfilePictureSettingsContentDialogUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/ProfilePictureSettingsDialog.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/ProfilePictureSettingsDialogUiTest.kt` | 5 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/RemoveProfilePictureConfirmationContentDialog.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/RemoveProfilePictureConfirmationContentDialogUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/RemoveProfilePictureConfirmationDialog.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/RemoveProfilePictureConfirmationDialogUiTest.kt` | 2 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/SyncStatusInfoContentDialog.kt` | 2 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/SyncStatusInfoContentDialogUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/SyncStatusInfoDialog.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/dialog/SyncStatusInfoDialogUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/screen/SummaryScreen.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/screen/SummaryScreenUiTest.kt` | 8 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/AnimatedSyncStatusText.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/AnimatedSyncStatusTextUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/DistributionView.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/DistributionViewUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/GradeTextView.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/GradeTextViewUiTest.kt` | 2 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/ProfilePictureEditButton.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/ProfilePictureEditButtonUiTest.kt` | 0 | MISSING TEST |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/ProfilePictureView.kt` | 3 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/ProfilePictureViewUiTest.kt` | 5 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/StatusCardItemView.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/StatusCardItemViewUiTest.kt` | 2 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryContentView.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryContentViewUiTest.kt` | 16 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryFailedView.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryFailedViewUiTest.kt` | 2 | PASS threshold (2) |
| `summary/src/commonMain/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryItems.kt` | 1 | `summary/src/commonTest/kotlin/com/gdavidpb/tuindice/summary/ui/view/SummaryItemsUiTest.kt` | 2 | PASS threshold (2) |

### `wizard`

| File | @Composable | Expected test | when_ cases | Status |
|---|---:|---|---:|---|
| `wizard/src/commonMain/kotlin/com/gdavidpb/tuindice/wizard/ui/view/CoachmarkBubble.kt` | 1 | `wizard/src/commonTest/kotlin/com/gdavidpb/tuindice/wizard/ui/view/CoachmarkBubbleUiTest.kt` | 0 | MISSING TEST |
| `wizard/src/commonMain/kotlin/com/gdavidpb/tuindice/wizard/ui/view/CoachmarkOverlayHost.kt` | 2 | `wizard/src/commonTest/kotlin/com/gdavidpb/tuindice/wizard/ui/view/CoachmarkOverlayHostUiTest.kt` | 6 | PASS threshold (2) |
