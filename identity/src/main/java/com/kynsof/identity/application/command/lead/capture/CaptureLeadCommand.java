package com.kynsof.identity.application.command.lead.capture;

import com.kynsof.identity.domain.dto.enumType.ELeadSource;
import com.kynsof.identity.domain.dto.enumType.ELeadType;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CaptureLeadCommand implements ICommand {

    private UUID id;
    private ELeadType type;
    private String businessName;
    private String contactName;
    private String email;
    private String phone;
    private String city;
    private String specialty;
    private Integer employeeCount;
    private String leadMessage;
    private ELeadSource source;

    public CaptureLeadCommand(CaptureLeadRequest request) {
        this.id = UUID.randomUUID();
        this.type = request.getType();
        this.businessName = request.getBusinessName();
        this.contactName = request.getContactName();
        this.email = request.getEmail();
        this.phone = request.getPhone();
        this.city = request.getCity();
        this.specialty = request.getSpecialty();
        this.employeeCount = request.getEmployeeCount();
        this.leadMessage = request.getMessage();
        this.source = request.getSource();
    }

    public static CaptureLeadCommand fromRequest(CaptureLeadRequest request) {
        return new CaptureLeadCommand(request);
    }

    @Override
    public ICommandMessage getMessage() {
        return new CaptureLeadMessage(id);
    }
}
