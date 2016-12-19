<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("loginTitle",realm.name)}
    <#elseif section = "header">
        Oops ... an SMS could not be sent
    <#elseif section = "form">
        <form id="kc-totp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label class="${properties.kcLabelClass!}">Please contact your system administrator</label>
                </div>

            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>
            </div>
        </form>
        <#if client?? && client.baseUrl?has_content>
            <p><a id="backToApplication" href="${client.baseUrl}">${msg("backToApplication")}</a></p>
        </#if>
    </#if>
</@layout.registrationLayout>