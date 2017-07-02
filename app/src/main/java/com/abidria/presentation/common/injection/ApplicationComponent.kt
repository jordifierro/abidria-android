package com.abidria.presentation.common.injection

import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.experience.ExperienceListActivity
import com.abidria.presentation.experience.ExperienceMapActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, DataModule::class))
interface ApplicationComponent {

    fun inject(experienceListActivity: ExperienceListActivity)
    fun inject(experienceMapActivity: ExperienceMapActivity)
}
