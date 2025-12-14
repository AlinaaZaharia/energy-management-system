package com.a1.demo.services;


import com.a1.demo.dtos.UserDTO;
import com.a1.demo.dtos.UserDetailsDTO;
import com.a1.demo.dtos.builders.UserBuilder;
import com.a1.demo.entities.User;
import com.a1.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.a1.demo.messaging.SyncEvent;
import com.a1.demo.messaging.SyncEventPublisher;
import com.a1.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final SyncEventPublisher syncEventPublisher;

    @Autowired
    public UserService(UserRepository userRepository, SyncEventPublisher syncEventPublisher) {
        this.userRepository = userRepository;
        this.syncEventPublisher = syncEventPublisher;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public List<UserDetailsDTO> findAllWithDetails(){
        return userRepository.findAll().stream()
                .map(UserBuilder::toUserDetailsDTO)
                .toList();
    }

    public UserDetailsDTO findUserById(UUID id) {
        Optional<User> prosumerOptional = userRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return UserBuilder.toUserDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(UserDetailsDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername()))
            throw new IllegalArgumentException("username already exists");
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("email already exists");

        User user = UserBuilder.toEntity(dto);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());

        SyncEvent event = new SyncEvent(
                SyncEvent.EntityType.USER,
                SyncEvent.ActionType.CREATED,
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
        syncEventPublisher.publishUserCreated(event);

        return user.getId();
    }

    public void update(UUID id, UserDetailsDTO dto){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User with id "+ id + " not found."));
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setDateOfBirth(dto.getDateOfBirth());
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was updated in db", user.getId());

        SyncEvent event = new SyncEvent(
                SyncEvent.EntityType.USER,
                SyncEvent.ActionType.UPDATED,
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
        syncEventPublisher.publishUserUpdated(event);
    }

    public void delete(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        userRepository.delete(user);
        LOGGER.debug("User with id {} was deleted from db", id);

        SyncEvent event = new SyncEvent(
                SyncEvent.EntityType.USER,
                SyncEvent.ActionType.DELETED,
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
        syncEventPublisher.publishUserDeleted(event);
    }
}