package com.tencent.tsf.femas.entity.registry.eureka;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Functional description
 *
 * @author Leo
 * @date 2019-12-18
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaApplication {

    private Application applications;

    public Application getApplications() {
        return applications;
    }

    public void setApplications(Application applications) {
        this.applications = applications;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Application {

        //        @JsonProperty("versions__delta")
//        private String versions;
//        @JsonProperty("apps__hashcode")
//        private String hashcode;
        private List<EurekaService> application;

        public List<EurekaService> getApplication() {
            return application;
        }

        public void setApplication(List<EurekaService> application) {
            this.application = application;
        }
    }

}
