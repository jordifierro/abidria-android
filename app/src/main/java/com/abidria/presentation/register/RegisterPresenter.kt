package com.abidria.presentation.register

import android.arch.lifecycle.LifecycleObserver
import com.abidria.data.auth.AuthRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class RegisterPresenter @Inject constructor(private val authRepository: AuthRepository,
                                            private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: RegisterView

    fun doneButtonClick() {
        view.showLoader()
        view.blockDoneButton(true)
        authRepository.register(view.getUsername(), view.getEmail())
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({
                    view.hideLoader()
                    view.blockDoneButton(false)
                    if (it.isSuccess()) {
                        view.showMessage("Successfully registered!\n Check your email to finalize the process")
                        view.finish()
                    } else view.showMessage(it.error!!.message!!)
                })
    }
}
