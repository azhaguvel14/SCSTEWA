package `in`.aibobscstewa.app

import android.app.Application
import com.google.android.material.color.DynamicColors

class AIBOBApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
