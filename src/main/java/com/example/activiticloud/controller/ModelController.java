package com.example.activiticloud.controller;
import org.activiti.editor.constants.ModelDataJsonConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_ID;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

@RestController
@RequestMapping("model")
public class ModelController {
        @Autowired
        private RepositoryService repositoryService;

        @Autowired
        private ObjectMapper objectMapper;

    /**
     * 用于创建model
     * @param request
     * @param response
     */
    @RequestMapping("create")
    public void createModel(HttpServletRequest request, HttpServletResponse response){
        try{
            String modelName = "modelName";
            String modelKey = "modelKey";
            String description = "description";

            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            RepositoryService repositoryService = processEngine.getRepositoryService();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, modelName);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(modelName);
            modelData.setKey(modelKey);

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
        }catch (Exception e){
        }
    }

    /**
     * 用于获取model（流程图模板信息）
     * @param modelId
     * @return
     */
    @RequestMapping(value="/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
        public ObjectNode getEditorJson(@PathVariable String modelId) {
            ObjectNode modelNode = null;

            Model model = repositoryService.getModel(modelId);

            if (model != null) {
                try {
                    if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                        modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                    } else {
                        modelNode = objectMapper.createObjectNode();
                        modelNode.put(MODEL_NAME, model.getName());
                    }
                    modelNode.put(MODEL_ID, model.getId());
                    ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(
                            new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                    modelNode.put("model", editorJsonNode);

                } catch (Exception e) {
                   // LOGGER.error("Error creating model JSON", e);
                    throw new ActivitiException("Error creating model JSON", e);
                }
            }
            return modelNode;
        }

}
