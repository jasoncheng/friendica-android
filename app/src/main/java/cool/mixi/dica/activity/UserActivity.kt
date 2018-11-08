package cool.mixi.dica.activity

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user.*
import cool.mixi.dica.App
import cool.mixi.dica.R
import cool.mixi.dica.adapter.StatusesAdapter
import cool.mixi.dica.bean.Consts
import cool.mixi.dica.bean.Status
import cool.mixi.dica.bean.User
import cool.mixi.dica.util.ApiService
import cool.mixi.dica.util.IStatusDataSouce
import cool.mixi.dica.util.StatusTimeline
import cool.mixi.dica.util.eLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import javax.net.ssl.HttpsURLConnection

class UserActivity: BaseActivity(), IStatusDataSouce {

    var user: User? = null
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        handleIntent()
    }

    private fun handleIntent(){
        user = intent.extras.get(Consts.EXTRA_USER) as User
        userId = intent.getStringExtra(Consts.EXTRA_USER_ID)

        if(user != null){
            initLoad()
            return
        }

        if(userId == null && user == null){
            App.instance.toast(getString(R.string.user_not_found))
            finish()
            return
        }

        ApiService.create().usersShow(userId!!).enqueue(
            CallbackUser(
                this
            )
        )
    }

    class CallbackUser(activity: UserActivity): Callback<User> {
        private val ref = WeakReference<UserActivity>(activity)
        private val errorMsg = ref.get()!!.getString(R.string.common_error)
        override fun onFailure(call: Call<User>, t: Throwable) {
            eLog(t.message.toString())
        }

        override fun onResponse(call: Call<User>, response: Response<User>) {
            if(ref.get() == null){
                return
            }

            if(response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED){
                App.instance.toast(errorMsg.format(response.errorBody().toString()))
                return
            }

            ref.get()!!.user = response.body()
            ref.get()!!.initLoad()
        }
    }

    fun initLoad(){
        stl = StatusTimeline(this, rv_statuses_list, home_srl, this).init()
        (rv_statuses_list.adapter as StatusesAdapter).ownerInfo = user
        stl?.loadMore(this)
    }

    override fun loaded(data: List<Status>) {
        if(data == null) return
        data.forEach {
            stl?.add(it)
        }
        rv_statuses_list.adapter.notifyDataSetChanged()
    }

    override fun sourceOld(): Call<List<Status>>? {
        return ApiService.create().statusUserTimeline(user?.id!!, "","${stl?.maxId}")
    }

    override fun sourceNew(): Call<List<Status>>? {
        return ApiService.create().statusUserTimeline(user?.id!!,"${stl?.sinceId}", "")
    }
}