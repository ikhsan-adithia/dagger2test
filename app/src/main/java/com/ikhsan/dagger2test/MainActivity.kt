package com.ikhsan.dagger2test

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.ikhsan.dagger2test.databinding.ActivityMainBinding
import dagger.*
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: MyViewModel

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RootApp.mainComponent.activityViewModelComponentBuilder()
            .componentActivity(this)
            .build()
            .inject(this)

        binding.root.setOnClickListener {
            viewModel.setState((viewModel.state.value?.plus(1) ?: 0).toString())
        }

        viewModel.state.observe(this) { str ->
            binding.tv.text = str
        }
    }
}

@Singleton
@Component
interface MainComponent {
    fun activityViewModelComponentBuilder(): ActivityViewModelComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope

@Module
class ActivityViewModelModule {
    @Provides
    @ActivityScope
    fun provideMyViewModel(activity: ComponentActivity, repository: Repository):
            MyViewModel {
        return ViewModelProvider(
            activity.viewModelStore,
            MyViewModelFactory(activity, repository, activity.intent.extras)
        )[MyViewModel::class.java]
    }
}

@ActivityScope
@Subcomponent(modules = [ActivityViewModelModule::class])
interface ActivityViewModelComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun componentActivity(activity: ComponentActivity): Builder
        fun build(): ActivityViewModelComponent
    }

    fun inject(activity: MainActivity)
}

class MyViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: Repository,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {
        return MyViewModel(
            repository, handle
        ) as T
    }
}

class MyViewModel(
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LifecycleObserver {

    companion object {
        const val KEY = "KEY"
    }

    private val showTextLiveData = savedStateHandle.getLiveData<String>(KEY)

    private val _state = MutableLiveData("")
    val state get(): LiveData<String> = _state

    val showTextDataNotifier: LiveData<String>
        get() = showTextLiveData

    init {
        fetchValue()
    }

    private fun fetchValue() {
        showTextLiveData.value = repository.getMessage()
    }

    fun setState(str: String) {
        _state.value = str
    }
}

class Repository @Inject constructor(): IRepository {
    override fun getMessage() = "From Repository"
}

interface IRepository {
    fun getMessage(): String
}