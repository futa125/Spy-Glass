import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.piratesofcode.spyglass.model.user.Employee
import com.piratesofcode.spyglass.model.user.UserRole
import com.piratesofcode.spyglass.viewmodels.DocumentsViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

class DocumentsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var documentsViewModel: DocumentsViewModel

    private lateinit var result: Unit

    @Before
    fun setup() {
        documentsViewModel = Mockito.mock(DocumentsViewModel::class.java)
    }

    //error handling
    @Test
    fun getDocumentsByFakeUser() {
        val user = Employee("testId", "testEmail@test.com", "ime", "prezime", UserRole.EMPLOYEE)

        result = documentsViewModel.getDocumentsByUser(user)
        Assert.assertEquals(result, Unit)
    }

    //init
    @Test
    fun getDocumentsLiveDataTest() {
        Assert.assertNull(documentsViewModel.getDocumentsLiveData())
    }
}