package ru.profapp.RanobeReader.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ru.profapp.RanobeReader.Fragments.HistoryPageFragment
import ru.profapp.RanobeReader.R



class HistoryFragmentPagerAdapter(fm: FragmentManager, val context: Context) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return HistoryPageFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.ranobes)
            1 -> context.getString(R.string.chapters)
            else -> context.getString(R.string.error)
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        val f = `object` as HistoryPageFragment
        f.update()

        return super.getItemPosition(`object`)
    }
}