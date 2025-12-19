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
    public BusinessInfo updateBusinessInfo(BusinessInfoDTO dto) {
        BusinessInfo info = getBusinessInfo().orElseGet(BusinessInfo::new);

        // Map BusinessInfo fields
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

        // Map owner (representative)
        Person owner;
        if (info.getOwner() != null) {
            owner = info.getOwner();
        } else if (dto.getOwnerCitizenId() != null && !dto.getOwnerCitizenId().isBlank()) {
            owner = personRepository.findById(dto.getOwnerCitizenId()).orElseGet(Person::new);
            owner.setCitizenId(dto.getOwnerCitizenId());
        } else {
            owner = new Person();
        }

        if (dto.getRepresentativeName() != null) owner.setFullName(dto.getRepresentativeName());
        if (dto.getAddress() != null) owner.setAddress(dto.getAddress());
        if (dto.getPhone() != null) owner.setPhone(dto.getPhone());
        owner.setFax(dto.getFax());
        // Tránh cập nhật email vì liên kết User <-> Person dựa trên email (FK)
        owner.setWebsite(dto.getWebsite());

        // Persist owner first (email unique constraint)
        owner = personRepository.save(owner);
        info.setOwner(owner);

        return businessInfoRepository.save(info);
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

        if (info.getOwner() != null) {
            dto.setOwnerCitizenId(info.getOwner().getCitizenId());
            dto.setRepresentativeName(info.getOwner().getFullName());
            dto.setAddress(info.getOwner().getAddress());
            dto.setPhone(info.getOwner().getPhone());
            dto.setFax(info.getOwner().getFax());
            dto.setEmail(info.getOwner().getEmail());
            dto.setWebsite(info.getOwner().getWebsite());
        }
        return dto;
    }
}
