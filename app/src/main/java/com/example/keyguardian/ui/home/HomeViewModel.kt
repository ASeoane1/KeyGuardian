import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId

    fun updateToken(token: String) {
        _token.value = token
    }

    fun updateUserId(userId: String) {
        _userId.value = userId
    }
}
