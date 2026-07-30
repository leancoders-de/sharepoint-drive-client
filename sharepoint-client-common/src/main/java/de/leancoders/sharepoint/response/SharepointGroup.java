package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharepointGroup {

    @JsonProperty("id")
    private String id;

    @JsonProperty("deletedDateTime")
    private OffsetDateTime deletedDateTime;

    @JsonProperty("classification")
    private String classification;

    @JsonProperty("createdDateTime")
    private OffsetDateTime createdDateTime;

    @JsonProperty("description")
    private String description;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("expirationDateTime")
    private OffsetDateTime expirationDateTime;

    @JsonProperty("groupTypes")
    private List<String> groupTypes;

    @JsonProperty("isAssignableToRole")
    private Boolean isAssignableToRole;

    @JsonProperty("mail")
    private String mail;

    @JsonProperty("mailEnabled")
    private Boolean mailEnabled;

    @JsonProperty("mailNickname")
    private String mailNickname;

    @JsonProperty("membershipRule")
    private String membershipRule;

    @JsonProperty("membershipRuleProcessingState")
    private String membershipRuleProcessingState;

    @JsonProperty("onPremisesLastSyncDateTime")
    private OffsetDateTime onPremisesLastSyncDateTime;

    @JsonProperty("onPremisesSecurityIdentifier")
    private String onPremisesSecurityIdentifier;

    @JsonProperty("onPremisesSyncEnabled")
    private Boolean onPremisesSyncEnabled;

    @JsonProperty("preferredDataLocation")
    private String preferredDataLocation;

    @JsonProperty("preferredLanguage")
    private String preferredLanguage;

    @JsonProperty("proxyAddresses")
    private List<String> proxyAddresses;

    @JsonProperty("renewedDateTime")
    private OffsetDateTime renewedDateTime;

    @JsonProperty("resourceBehaviorOptions")
    private List<String> resourceBehaviorOptions;

    @JsonProperty("resourceProvisioningOptions")
    private List<String> resourceProvisioningOptions;

    @JsonProperty("securityEnabled")
    private Boolean securityEnabled;

    @JsonProperty("theme")
    private String theme;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("onPremisesProvisioningErrors")
    private List<Object> onPremisesProvisioningErrors;

    @JsonProperty("serviceProvisioningErrors")
    private List<Object> serviceProvisioningErrors;

}