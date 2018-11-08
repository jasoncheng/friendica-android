package cool.mixi.dica.activity

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_status.*
import cool.mixi.dica.App
import cool.mixi.dica.R
import cool.mixi.dica.bean.Consts
import cool.mixi.dica.bean.Status
import cool.mixi.dica.util.ApiService
import cool.mixi.dica.util.IStatusDataSouce
import cool.mixi.dica.util.StatusTimeline
import retrofit2.Call
import java.util.*

class StatusActivity: BaseActivity(), IStatusDataSouce {

    private var statusId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)
        processIntent()
    }

    private fun processIntent(){
        if(intent == null) {return}
        statusId = intent.getIntExtra(Consts.ID_STATUS, 0)
        if(statusId == 0){
            App.instance.toast(getString(R.string.status_not_found))
        }

        stl = StatusTimeline(this, rv_statuses_list, home_srl, this).init()
        stl?.loadNewest(null)
    }


    override fun loaded(data: List<Status>) {
        Collections.reverse(data)
        stl?.clear()
        stl?.addAll(data!!)
        rv_statuses_list.adapter.notifyDataSetChanged()
    }

    override fun sourceOld(): Call<List<Status>>? {
        stl?.allLoaded = true
        return null
    }

    override fun sourceNew(): Call<List<Status>>? {
        return ApiService.create().statusShow(statusId!!, 1)
    }
}