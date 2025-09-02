package co.com.sti.api.mapper;

import co.com.sti.api.dto.CreateUserDTO;
import co.com.sti.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    CreateUserDTO toResponse(User user);
    User toModel(CreateUserDTO createUserDTO);

}
