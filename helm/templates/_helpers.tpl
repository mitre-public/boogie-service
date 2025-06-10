{{/*
Expand the name of the chart.
*/}}
{{- define "boogie-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "boogie-service.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Be explicit about namespace
*/}}
{{- define "boogie-service.namespace" -}}
{{- required "Must explicitly define namespace" .Values.namespace | quote }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "boogie-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "boogie-service.labels" -}}
helm.sh/chart: {{ include "boogie-service.chart" . }}
{{ include "boogie-service.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- if .Values.partof.app }}
app.kubernetes.io/part-of: {{ .Values.partof.app | quote }}
{{- end }}
{{- if .Values.partof.component }}
app.kubernetes.io/component: {{ .Values.partof.component | quote }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "boogie-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "boogie-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Hashed Config Map
*/}}
{{- define "boogie-service.configMapName" -}}
{{- printf "%s" (include "boogie-service.fullname" . ) }}
{{- end }}
