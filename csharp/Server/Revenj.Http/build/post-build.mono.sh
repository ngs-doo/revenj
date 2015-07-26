#!/bin/sh

exec cp \
	../../../Features/NGS.Features.Storage/bin/Mono/NGS.Features.Storage.dll* \
	../../../Features/NGS.Features.Mailer/bin/Debug/NGS.Features.Mailer.dll* \
	../../../Features/Revenj.Features.RestCache/bin/Release/Revenj.Features.RestCache.dll* \
	../../../Plugins/NGS.Plugins.DatabasePersistence.Postgres/bin/Mono/NGS.Plugins.DatabasePersistence.Postgres.dll* \
	../../../Plugins/Revenj.Plugins.Rest.Commands/bin/Mono/Revenj.Plugins.Rest.Commands.dll* \
	../../../Plugins/Revenj.Plugins.Server.Commands/bin/Mono/Revenj.Plugins.Server.Commands.dll* \
	../bin/Debug/

