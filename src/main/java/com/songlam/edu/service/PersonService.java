package com.songlam.edu.service;

import com.songlam.edu.dto.MeDTO;
import com.songlam.edu.entity.Person;
import com.songlam.edu.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional
    public Person updateMeInfo(MeDTO dto) {
        Person person = personRepository.findByEmail(dto.getEmail()).orElseGet(Person::new);

        person.setFullName(dto.getFullName());
        person.setDateOfBirth(dto.getDateOfBirth());
        person.setSex(dto.getSex());
        person.setNationality(dto.getNationality());
        person.setPlaceOfOrigin(dto.getPlaceOfOrigin());
        person.setPlaceOfResidence(dto.getPlaceOfResidence());
        person.setAddress(dto.getAddress());
        person.setPhone(dto.getPhone());

        return personRepository.save(person);
    }
}
