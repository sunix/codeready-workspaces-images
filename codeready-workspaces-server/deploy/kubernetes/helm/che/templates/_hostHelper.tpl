{{- define "cheHost" }}
    {{- if or (eq .Values.global.serverStrategy "default-host") (eq .Values.global.serverStrategy "single-host") }}
        {{- printf "%s" .Values.global.ingressDomain }}
    {{- else }}
        {{- if .Values.global.cheHost }}
            {{- .Values.global.cheHost }}
        {{- else }}
            {{- printf "che-%s.%s" .Release.Namespace .Values.global.ingressDomain }}
        {{- end }}
    {{- end }}
{{- end }}
