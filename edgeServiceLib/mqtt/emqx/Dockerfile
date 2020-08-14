FROM	emqx/emqx
ENV EMQX_LOADED_PLUGINS="emqx_recon,emqx_retainer,emqx_management,emqx_dashboard,emqx_web_hook"
#ENV	EMQX_LOADED_PLUGINS="emqx_recon,emqx_retainer,emqx_management,emqx_dashboard,emqx_web_hook,emqx_auth_http"
#ENV EMQX_LOADED_PLUGINS="emqx_recon,emqx_retainer,emqx_management,emqx_dashboard,emqx_auth_mysql"
#ADD	emqx_auth_mysql.conf	etc/plugins/
ADD	emqx_web_hook.conf	etc/plugins/
#ADD	emqx_auth_http.conf	etc/plugins/
ADD	emqx.conf	/etc/
