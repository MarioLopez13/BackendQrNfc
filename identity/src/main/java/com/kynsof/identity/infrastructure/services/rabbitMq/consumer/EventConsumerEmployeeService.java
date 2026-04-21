//package com.kynsof.identity.infrastructure.services.rabbitMq.consumer;
//
//import com.kynsof.identity.application.command.auth.registrySystemUser.UserSystemKycloackRequest;
//import com.kynsof.identity.domain.dto.UserStatus;
//import com.kynsof.identity.domain.dto.UserSystemDto;
//import com.kynsof.identity.domain.interfaces.service.IAuthService;
//
//import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
//import com.kynsof.identity.infrastructure.services.rabbitMq.dto.RabbitMqEmployeeDto;
//import com.kynsof.share.core.domain.EUserType;
//
//import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//public class EventConsumerEmployeeService {
//
//    private final IUserSystemService service;
//    private final IAuthService authService;
//
//    public EventConsumerEmployeeService(IUserSystemService service, IAuthService authService) {
//        this.service = service;
//        this.authService = authService;
//    }
//
//    @Transactional
//    @RabbitListener(queues = "employee.queue.identity")
//    public void handleCompanyEvent(RabbitMqEmployeeDto event) {
//        UserSystemDto userSystemDto = new UserSystemDto(
//                event.getId(),
//                event.getEmail(),
//                event.getEmail(),
//                event.getFirstName(),
//                event.getLastName(),
//                UserStatus.ACTIVE,
//                null
//        );
//        userSystemDto.setSelectedBusiness(event.getBusiness());
//        try {
//            service.findById(event.getId()); // Lanza excepción si no existe
//            service.update(userSystemDto);
//        } catch (BusinessNotFoundException e) {
//
////            UserSystemKycloackRequest userSystemRequest = new UserSystemKycloackRequest(
////                    event.getEmail(),
////                    event.getEmail(),
////                    event.getFirstName(),
////                    event.getLastName(),
////                    "Ecuador.*2014",
////                    EUserType.UNDEFINED
////            );
////            String userId = authService.registerUserSystem(userSystemRequest, true);
////            userSystemDto.setKeyCloakId(UUID.fromString(userId));
//
//            service.create(userSystemDto);
//        }
//    }
//}
