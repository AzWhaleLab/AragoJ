package com.aragoj.base

import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import de.saxsys.mvvmfx.SceneLifecycle
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

abstract class ViewModel : de.saxsys.mvvmfx.ViewModel, SceneLifecycle, LifecycleScopeProvider<ViewModel.ViewModelEvent> {

    private val lifecycleEvents = BehaviorSubject.create<ViewModelEvent>()

    enum class ViewModelEvent {
        CREATED, DESTROYED
    }

    override fun onViewAdded() {
        lifecycleEvents.onNext(ViewModelEvent.CREATED)
    }

    override fun onViewRemoved() {
        lifecycleEvents.onNext(ViewModelEvent.DESTROYED)
    }

    override fun lifecycle(): Observable<ViewModelEvent> {
        return lifecycleEvents.hide()
    }

    override fun correspondingEvents(): CorrespondingEventsFunction<ViewModelEvent> {
        return CORRESPONDING_EVENTS
    }

    override fun peekLifecycle(): ViewModelEvent? {
        return lifecycleEvents.value
    }

    companion object {
        private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<ViewModelEvent> { event ->
            when (event) {
                ViewModelEvent.CREATED -> ViewModelEvent.DESTROYED
                else -> throw LifecycleEndedException(
                        "Cannot bind to ViewModel lifecycle after onDestroyed.")
            }
        }
    }
}