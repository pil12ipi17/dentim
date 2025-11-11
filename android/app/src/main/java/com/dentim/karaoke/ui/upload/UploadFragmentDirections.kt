package com.dentim.karaoke.ui.upload

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections

/**
 * Navigation directions for UploadFragment
 */
object UploadFragmentDirections {
    fun actionUploadToHome(): NavDirections {
        return ActionOnlyNavDirections(android.R.id.home) // Placeholder - actual ID should be defined in nav graph
    }
    
    fun actionUploadToProcessing(processingId: String): NavDirections {
        return ActionOnlyNavDirections(android.R.id.home) // Placeholder - actual ID should be defined in nav graph
    }
}