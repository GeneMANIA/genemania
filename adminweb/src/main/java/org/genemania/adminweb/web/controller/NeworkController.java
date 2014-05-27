package org.genemania.adminweb.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.genemania.adminweb.entity.Network;
import org.genemania.adminweb.exception.DatamartException;
import org.genemania.adminweb.service.NetworkService;
import org.genemania.adminweb.service.ValidationService;
import org.genemania.adminweb.web.model.AttributeMetadataForm;
import org.genemania.adminweb.web.model.NetworkForm;
import org.genemania.adminweb.web.model.NetworkTN;
import org.genemania.adminweb.web.model.OrganismForm;
import org.genemania.adminweb.web.model.OrganismTN;
import org.genemania.adminweb.web.model.ViewModel;
import org.genemania.adminweb.web.service.OrganismService;
import org.genemania.adminweb.web.service.ServiceSupport;
import org.genemania.adminweb.web.service.TreeBuilderService;
import org.genemania.adminweb.web.service.FormProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/*
 * handle upload of data files
 *
 *  * store file in datastore
 *  * add new metadata entry
 *  * return id of new network in 'id' field
 *    of response
 */
@Controller
public class NeworkController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(NeworkController.class);

    @Autowired
    private OrganismService organismService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TreeBuilderService treeBuilderService;

    @Autowired
    private FormProcessorService formProcessorService;

    @RequestMapping("/upload")
    public ModelAndView about(HttpServletRequest request) {

        ViewModel model = ServiceSupport.generateModel(request);
        organismService.updateModel(model);
        System.out.println("upoad controller");
        return new ModelAndView("uptest", model);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addnetwork")
    @ResponseBody
    public Map<String, String> addNetwork(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("groupId") int groupId) throws DatamartException, IOException {

        logger.info("processing network file upload: Organism {} group {} file {}", organismId, groupId, file.getOriginalFilename());

        Network network = null;
        //fileService.putNetworkFile(organismId, filename, file.getInputStream());
        network = networkService.addNetwork(organismId, groupId, file.getOriginalFilename(), file.getInputStream());

        // node corresponding to the updated network ... just return
        // key for now but maybe we can pass back the node and update
        // tree on client without another reload() round trip?
        Map<String, String> responseMap = new HashMap<String, String>();
        NetworkTN networkTN = treeBuilderService.getNetworkTN(network);
        responseMap.put("key", networkTN.getKey());
        responseMap.put("error", "0");

        try {
            validationService.validateNetwork(network);
        }
        catch (DatamartException e) {
            responseMap.put("error", "1");
        }

        return responseMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/replacenetwork")
    @ResponseBody
    public Map<String, String> replaceNetwork(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("networkId") int networkId) throws DatamartException, IOException {

        logger.info("processing network file upload: Organism {} network {} file {}", organismId, networkId, file.getOriginalFilename());

        Network network = networkService.replaceNetwork(organismId, networkId, file.getOriginalFilename(), file.getInputStream());
        validationService.validateNetwork(network);

        Map<String, String> responseMap = new HashMap<String, String>();

        // anything we want to put in the response?
        //        responseMap.put("id", Integer.toString(id));

        return responseMap;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/replaceAttributeMetadata")
    @ResponseBody
    public Map<String, String> replaceAttributeMetadata(@RequestParam("files") MultipartFile file,
            @RequestParam("organismId") int organismId,
            @RequestParam("networkId") int networkId) throws DatamartException, IOException {

        logger.info("processing attribute metadata upload: Organism {} network {} file {}", organismId, networkId, file.getOriginalFilename());

        Network network = networkService.replaceAttributeMetadata(organismId, networkId, file.getOriginalFilename(), file.getInputStream());
        validationService.validateAttributeMetadata(network);

        Map<String, String> responseMap = new HashMap<String, String>();

        // anything we want to put in the response?
        //        responseMap.put("id", Integer.toString(id));

        return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/addorganism", params = "submitButton")
    public Map<String, String> addOrganism(@ModelAttribute("OrganismForm")
        OrganismForm organismForm, BindingResult result) throws DatamartException {

      logger.info("add organism");
      OrganismTN organismTN =  organismService.addOrganism(organismForm);
      Map<String, String> responseMap = new HashMap<String, String>();

      if (organismTN != null) {
          responseMap.put("key", organismTN.getKey());
      }
      else {
          responseMap.put("key", "");
      }
        return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/addorganism", params = "deleteButton")
    public Map<String, String> deleteOrganism(@ModelAttribute("OrganismForm")
        OrganismForm organismForm, BindingResult result) throws DatamartException {

      logger.info("delete organism");

      int organismId = Integer.parseInt(organismForm.getOrganismId());
      organismService.deleteOrganism(organismId);

      Map<String, String> responseMap = new HashMap<String, String>();
      return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/update")
    public String updateNetworkField(
            @RequestParam("organismId") int organismId,
            @RequestParam("networkId") int networkId,
            @RequestParam("id") String field,
            @RequestParam("value") String value) throws DatamartException {

        logger.info("update controller");
        value = networkService.updateNetwork(organismId, networkId, field, value);
        return value;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateNetwork", params = "submitButton")
    public String updateNetwork(@ModelAttribute("NetworkForm")
        NetworkForm networkForm, BindingResult result) throws DatamartException {

        logger.info("update controller: updateNetwork");
        formProcessorService.updateNetwork(networkForm);
        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateNetwork", params = "deleteButton")
    public String deleteNetwork(@ModelAttribute("NetworkForm")
        NetworkForm networkForm, BindingResult result) throws DatamartException {

        logger.info("update controller: deleteNetwork");

        int organismId = networkForm.getOrganismId();
        int networkId = networkForm.getNetworkId();

        logger.info("processing network delete: Organism {} network {} ", organismId, networkId);

        networkService.deleteNetwork(organismId, networkId);
        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateNetwork", params = "updateMetaButton")
    public Map<String, String> updateNetworkMetadata(@RequestParam("organismId") int organismId,
            @RequestParam("networkId") int networkId) throws DatamartException {

        Network network = networkService.updateNetworkMetadata(organismId, networkId);
        NetworkTN networkTN = treeBuilderService.getNetworkTN(network);

        Map<String, String> responseMap = new HashMap<String, String>();
        if (networkTN != null) {
            responseMap.put("key", networkTN.getKey());
        }
        else {
            responseMap.put("key", "");
        }

        return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "updateNetwork", params = "updateValidationButton")
    public Map<String, String> updateNetworkValidation(@RequestParam("organismId") int organismId,
            @RequestParam("networkId") int networkId) throws DatamartException {

        logger.info("revalidating network: organism {} network {}", organismId, networkId);
        Network network = networkService.getNetwork(organismId, networkId);
        validationService.validateNetwork(network);
        NetworkTN networkTN = treeBuilderService.getNetworkTN(network);

        Map<String, String> responseMap = new HashMap<String, String>();
        if (networkTN != null) {
            responseMap.put("key", networkTN.getKey());
        }
        else {
            responseMap.put("key", "");
        }

        return responseMap;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateAttributeMetadata", params = "submitButton")
    public String updateAttributeMetadata(@ModelAttribute("AttributeMetadataForm")
        AttributeMetadataForm form, BindingResult result) throws DatamartException {

        logger.info("update controller: updateAttributeMetadata");

        int organismId = form.getOrganismId();
        int networkId = form.getNetworkId();

        logger.info("Updating attribute metadata: organism {} network {}", organismId, networkId);
        formProcessorService.updateAdttributeMetadata(form);

        return null;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/updateAttributeMetadata", params = "deleteButton")
    public String deleteAttributeMetadataFile(@ModelAttribute("AttributeMetadataForm")
        AttributeMetadataForm form, BindingResult result) throws DatamartException {

        logger.info("update controller: deleteAttributeMetadataFile");

        int organismId = form.getOrganismId();
        int networkId = form.getNetworkId();

        networkService.deleteAttributeMetadata(organismId, networkId);

        return null;
    }

}
