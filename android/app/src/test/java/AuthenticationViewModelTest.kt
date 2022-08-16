import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.piratesofcode.spyglass.viewmodels.AuthenticationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class AuthenticationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authenticationViewModel: AuthenticationViewModel

    private lateinit var result: Unit

    @Before
    fun setup() {
        authenticationViewModel = mock(AuthenticationViewModel::class.java)
    }

    //error handling for viewModel functions
    @Test
    fun loginFakeUser() {
        val email = "unittest@test.com"
        val password = "123_456"

        result = authenticationViewModel.login(email, password)
        assertEquals(result, Unit)
    }

    @Test
    fun loginEmptyEmail() {
        val email = ""
        val password = "123_456"

        result = authenticationViewModel.login(email, password)
        assertEquals(result, Unit)
    }

    @Test
    fun registerEmptyEmail() {
        val email = ""
        val password = "123_456"

        result = authenticationViewModel.register(
            email,
            password,
            "ime",
            "prezime",
            "accountant",
            "offer"
        )
        assertEquals(result, Unit)
    }

    @Test
    fun logout() {
        result = authenticationViewModel.logout()
        assertEquals(result, Unit)
    }

}