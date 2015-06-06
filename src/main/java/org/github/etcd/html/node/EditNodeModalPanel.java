package org.github.etcd.html.node;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.github.etcd.browser.FormGroupBorder;
import org.github.etcd.rest.EtcdManager;
import org.github.etcd.rest.EtcdNode;

public class EditNodeModalPanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private final IModel<Boolean> updating;

    private Label title;
    private EtcdNodeForm form;

    @Inject
    private Provider<EtcdManager> etcdManager;

    public EditNodeModalPanel(String id, IModel<EtcdNode> model, IModel<Boolean> updatingModel) {
        super(id, model);
        this.updating = updatingModel;

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));

        add(title = new Label("title", new StringResourceModel("editModal.title.updating.${}", updating, "Edit Node")));
        title.setOutputMarkupId(true);

        add(form = new EtcdNodeForm("form", new CompoundPropertyModel<>(model)));

        add(new AjaxSubmitLink("save", form) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                etcdManager.get().saveOrUpdate(getModelObject(), updating.getObject());

                onNodeSaved(target);

                target.appendJavaScript("$('#" + EditNodeModalPanel.this.getMarkupId() + "').modal('hide');");
            }

            @Override
            protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onAfterSubmit(target, form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);

                target.add(form);
            }
        });

    }

    public class EtcdNodeForm extends Form<EtcdNode> {

        private static final long serialVersionUID = 1L;

        public EtcdNodeForm(String id, IModel<EtcdNode> model) {
            super(id, model);

            add(new FormGroupBorder("keyGroup", new ResourceModel("editModal.form.key.label", "Key")).add(new TextField<String>("key") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            }.setRequired(true)));

            add(new FormGroupBorder("valueGroup", new ResourceModel("editModal.form.value.label", "Value")) {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setVisible(!EtcdNodeForm.this.getModelObject().isDir());
                }
            }.add(new TextArea<String>("value") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setEnabled(!EtcdNodeForm.this.getModelObject().isDir());
                }
            }));

            add(new AjaxCheckBox("dir") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(EtcdNodeForm.this);
                }
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            });

            add(new Label("dirLabel", new ResourceModel("editModal.form.dir.label", "Directory")));

            add(new FormGroupBorder("ttlGroup", new ResourceModel("editModal.form.ttl.label", "Time to live")).add(new TextField<>("ttl")));

        }

    }

    public void onShowModal(AjaxRequestTarget target) {
        target.add(title, form);

        form.clearInput();
    }
    protected void onNodeSaved(AjaxRequestTarget target) {
    }
}