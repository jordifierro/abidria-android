package com.abidria.presentation.experience.show

import com.abidria.data.experience.Experience
import com.abidria.presentation.common.view.LoaderView

interface ExploreView : LoaderView {

    fun showRetry()
    fun hideRetry()
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
}
