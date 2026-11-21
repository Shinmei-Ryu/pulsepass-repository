package com.pulse.pass.mapper;

import com.pulse.pass.domain.User;
import com.pulse.pass.dto.response.UserResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "firstName", source = "userProfile.firstName")
    @Mapping(target = "lastName", source = "userProfile.lastName")
    @Mapping(target = "city", source = "userProfile.city")
    @Mapping(target = "birthDate", source = "userProfile.birthDate")
    UserResponse toResponse(User user);
}