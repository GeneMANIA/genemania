package org.genemania.adminweb.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.genemania.adminweb.web.model.OrganismTN;
import org.genemania.adminweb.web.model.TreeNode;
import org.genemania.adminweb.web.service.OrganismService;
import org.genemania.adminweb.web.service.TreeBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * return tree data structure used
 * to generate UI view.
 */
@Controller
public class TreeController extends BaseController {
    final Logger logger = LoggerFactory.getLogger(TreeController.class);

    @Autowired
    private OrganismService organismService;

    @Autowired
    private TreeBuilderService treeBuilderService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/organism/{organismId}")
    public List<TreeNode> list(@PathVariable int organismId, HttpSession session) {

        logger.info("org data controller with arg: {}", organismId);

        return treeBuilderService.getOrganismDataTree(organismId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/organism/all")
    public List<? extends TreeNode> listOrganisms(HttpSession session) {

        logger.info("all organisms tree controller ");

        List<OrganismTN> tree = organismService.getOrganismsTree();
        for (OrganismTN organismTN: tree) {
            organismTN.addChildren(treeBuilderService.getOrganismDataTree(organismTN.getId()));
        }

        return tree;
    }
}
