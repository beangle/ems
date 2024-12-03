[#ftl]
{"business":"${meta.business.name?js_string}","description": "${meta.description?js_string}","params": [[#list meta.params as param]{"id": "${param.id?js_string}","title": "${param.title?js_string}","description": "${param.description?js_string}"}[#if param_has_next],[/#if][/#list]]}
