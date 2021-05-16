package ru.mephi.birthday

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.mephi.birthday.database.Person
import ru.mephi.birthday.repository.Repository
import java.util.*

class PersonViewModel(private val repository: Repository) : ViewModel() {

    val people : LiveData<List<Person>> = repository.allPerson.asLiveData()
    val person : MutableLiveData<Person> = MutableLiveData()

    fun insert(person: Person)  {
        viewModelScope.launch {
            repository.insert(person)}
    }

    fun update(person: Person)  {
        viewModelScope.launch {
            repository.update(person)}
    }

    fun delete(person: Person)  {
        viewModelScope.launch {
            repository.delete(person)}
    }

    fun getPersonById(uuid: UUID) = viewModelScope.launch {
            val prs = repository.getPerson(uuid)
            person.postValue(prs)
    }
}

class PersonViewModelFactory (private val repository: Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}