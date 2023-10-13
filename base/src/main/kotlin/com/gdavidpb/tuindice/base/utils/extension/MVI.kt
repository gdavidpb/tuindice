package com.gdavidpb.tuindice.base.utils.extension

interface ViewInput
interface ViewOutput

interface ViewStepState : ViewState

interface ViewState : ViewOutput
interface ViewAction : ViewInput
interface ViewEvent : ViewOutput