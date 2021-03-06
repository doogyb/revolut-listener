package me.doogyb.ledgerio.screens.settings

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import me.doogyb.ledgerio.ResetBudgetReceiver
import me.doogyb.ledgerio.database.AppDatabase
import me.doogyb.ledgerio.domain.Amount
import me.doogyb.ledgerio.repository.LedgerRepository

//const val TAG = "SettingsViewModel"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val ledger = LedgerRepository(AppDatabase.getInstance(application))
    private val context = application.applicationContext

    private val sharedPreferences : SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = sharedPreferences.edit()

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent


    // Performs inserts on the budget and amount tables, also sets the daily limit as this
    // needs to be recomputed on the new budget
    fun onSaveBudget(budget: Int) {
        ledger.setBudget(Amount(budget, 0), context)
        ledger.clearTodaysSpend()
        saveDailyLimit(budget, sharedPreferences.getString("interval_preference", "1") ?: "1")
    }
    // Saves the interval to SharedPreference, also sets the daily limit as this needs to be
    // recomputed using the new interval
    fun onSaveInterval(interval: Int) {
        // Set alarm to reset the Budget every interval days
        setAlarm(interval)
        saveDailyLimit(sharedPreferences.getString("budget_preference", "0") ?: "0", interval)
    }

    // Function gets called when Budget and Interval are changed, i.e.
    // When onSaveBudget and onSaveInterval are called.
    private fun saveDailyLimit(budget: Int, interval: Int) {
        editor.putFloat("daily_limit", budget.toFloat() / interval)
        editor.commit()
    }
    private fun saveDailyLimit(budget: String, interval: Int) {
        saveDailyLimit(Integer.parseInt(budget), interval)
    }
    private fun saveDailyLimit(budget: Int, interval: String) {
        saveDailyLimit(budget, Integer.parseInt(interval))
    }

    // Sets an Alarm to reset the budget every interval days.
    private fun setAlarm(intervalAmount: Int) {
        alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, ResetBudgetReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        // TODO use budgetInterval - in particular implement calendar picker
        alarmMgr?.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            AlarmManager.INTERVAL_DAY * intervalAmount,
            alarmIntent
        )
    }
}