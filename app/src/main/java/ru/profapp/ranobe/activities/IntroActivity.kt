package ru.profapp.ranobe.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.paolorotolo.appintro.AppIntro
import ru.profapp.ranobe.BuildConfig
import ru.profapp.ranobe.R
import ru.profapp.ranobe.fragments.intro.IntroFragment

class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(IntroFragment.newInstance(R.layout.fragment_intro_1))
        addSlide(IntroFragment.newInstance(R.layout.fragment_intro_2))
        addSlide(IntroFragment.newInstance(R.layout.fragment_intro_3))
        addSlide(IntroFragment.newInstance(R.layout.fragment_intro_4))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        closeIntro()
    }

    private fun closeIntro() {
        val getPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        getPrefs.edit().putBoolean(BuildConfig.INTRO_KEY, false).apply()
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        closeIntro()
        // Do something when users tap on Done button.
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.
    }
}