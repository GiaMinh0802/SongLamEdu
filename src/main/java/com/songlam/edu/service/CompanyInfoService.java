package com.songlam.edu.service;

import com.songlam.edu.dto.BusinessInfoDTO;
import com.songlam.edu.entity.BusinessInfo;
import com.songlam.edu.entity.Person;
import com.songlam.edu.repository.BusinessInfoRepository;
import com.songlam.edu.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyInfoService {

    private final BusinessInfoRepository businessInfoRepository;
    private final PersonRepository personRepository;

    public CompanyInfoService(BusinessInfoRepository businessInfoRepository, PersonRepository personRepository) {
        this.businessInfoRepository = businessInfoRepository;
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public Optional<BusinessInfo> getBusinessInfo() {
        return businessInfoRepository.findAll().stream().findFirst();
    }

    @Transactional
    public void updateBusinessInfo(BusinessInfoDTO dto) {
        BusinessInfo info = getBusinessInfo().orElseGet(BusinessInfo::new);

        info.setCompanyCode(dto.getCompanyCode());
        info.setBusinessName(dto.getBusinessName());
        info.setAbbreviatedName(dto.getAbbreviatedName());
        info.setBusinessType(dto.getBusinessType());
        info.setTaxCode(dto.getTaxCode());
        info.setEstablishedDate(dto.getEstablishedDate());
        info.setBusinessRegistrationCode(dto.getBusinessRegistrationCode());
        info.setIssuedDate(dto.getIssuedDate());
        info.setIssuedPlace(dto.getIssuedPlace());
        info.setRepresentativeTitle(dto.getRepresentativeTitle());
        info.setFax(dto.getFax());
        info.setWebsite(dto.getWebsite());

        Person owner;
        if (info.getPerson() != null) {
            owner = info.getPerson();
        } else if (dto.getOwnerCitizenId() != null && !dto.getOwnerCitizenId().isBlank()) {
            owner = personRepository.findById(dto.getOwnerCitizenId()).orElseGet(Person::new);
            owner.setCitizenId(dto.getOwnerCitizenId());
        } else {
            owner = new Person();
        }

        if (dto.getRepresentativeName() != null) owner.setFullName(dto.getRepresentativeName());
        if (dto.getAddress() != null) owner.setAddress(dto.getAddress());
        if (dto.getPhone() != null) owner.setPhone(dto.getPhone());
        if (dto.getEmail() != null) owner.setEmail(dto.getEmail());

        owner = personRepository.save(owner);
        info.setPerson(owner);

        businessInfoRepository.save(info);
    }

    public BusinessInfoDTO toDTO(BusinessInfo info) {
        BusinessInfoDTO dto = new BusinessInfoDTO();
        if (info == null) return dto;
        dto.setCompanyCode(info.getCompanyCode());
        dto.setBusinessName(info.getBusinessName());
        dto.setAbbreviatedName(info.getAbbreviatedName());
        dto.setBusinessType(info.getBusinessType());
        dto.setTaxCode(info.getTaxCode());
        dto.setEstablishedDate(info.getEstablishedDate());
        dto.setBusinessRegistrationCode(info.getBusinessRegistrationCode());
        dto.setIssuedDate(info.getIssuedDate());
        dto.setIssuedPlace(info.getIssuedPlace());
        dto.setRepresentativeTitle(info.getRepresentativeTitle());
        dto.setFax(info.getFax());
        dto.setWebsite(info.getWebsite());

        if (info.getPerson() != null) {
            dto.setOwnerCitizenId(info.getPerson().getCitizenId());
            dto.setRepresentativeName(info.getPerson().getFullName());
            dto.setAddress(info.getPerson().getAddress());
            dto.setPhone(info.getPerson().getPhone());
            dto.setEmail(info.getPerson().getEmail());
        }
        return dto;
    }
}
