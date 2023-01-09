package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.databinding.ActivityMainBinding
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var userName: String
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var listAdapter: RepositoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
        showUserName()
        setupRetrofit()
    }

    private fun setupListeners() {
        listaRepositories = binding.rvListRepositories
        binding.btnConfirm.setOnClickListener {
            userName = binding.etUseName.text.toString()
            saveUserLocal(userName)
            getAllReposByUserName()
            binding.etUseName.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }
    }

    private fun saveUserLocal(name: String) {
        val savePrefs = getPreferences(Context.MODE_PRIVATE) ?: return
        savePrefs.let {
            val editor = it.edit()
            editor.putString(getString(R.string.nameKey), name)
            editor.apply()
        }
    }

    private fun showUserName() {
        val savePrefs = getPreferences(Context.MODE_PRIVATE)
        val name = savePrefs.getString(getString(R.string.nameKey), "1")
        if (name != null) binding.etUseName.setText(name)
    }

    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi = retrofit.create(GitHubService::class.java)
        //Documentacao oficial do retrofit - https://square.github.io/retrofit/
    }

    fun getAllReposByUserName() {
        githubApi.getAllRepositoriesByUser(userName).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        setupAdapter(it)
                    }
                } else {
                    toast(getString(R.string.MSG))
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                toast(getString(R.string.MSG))
            }
        })
    }

    fun setupAdapter(list: List<Repository>) {
        listAdapter = RepositoryAdapter(list)
        listaRepositories.apply {
            adapter = listAdapter

        }
        listAdapter.btnShareLister = {
            shareRepositoryLink(it.htmlUrl)
        }
        listAdapter.itemLister = {
            openBrowser(it.htmlUrl)
        }
    }

    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

}