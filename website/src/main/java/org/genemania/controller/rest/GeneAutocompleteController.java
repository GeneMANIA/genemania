package org.genemania.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.genemania.controller.rest.GeneValidationController.ValidationRequest;
import org.genemania.exception.ApplicationException;
import org.genemania.service.GeneService;
import org.genemania.service.GeneService.AutocompleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GeneAutocompleteController {

	@Autowired
	private GeneService geneService;

	public class AutocompleteRequest {
		private Integer organism;
		private String gene;

		public Integer getOrganism() {
			return organism;
		}

		public void setOrganism(Integer organism) {
			this.organism = organism;
		}

		public String getGene() {
			return gene;
		}

		public void setGene(String gene) {
			this.gene = gene;
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/gene_autocompletion")
	@ResponseBody
	public AutocompleteResult list(HttpSession session, HttpServletRequest req)
			throws ApplicationException {
		// TODO
		return null;

	}

	public GeneService getGeneService() {
		return geneService;
	}

	public void setGeneService(GeneService geneService) {
		this.geneService = geneService;
	}


}
