package co.com.sti.api;

import co.com.sti.api.config.TaskPath;
import co.com.sti.api.dto.CreateUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    beanClass = Handler.class,
                    beanMethod = "registerUserEntryPoint",
                    operation = @Operation(
                            operationId = "saveUser",
                            summary = "Registrar un nuevo usuario",
                            description = "Recibe un objeto CreateUserDTO y guarda el nuevo usuario en el sistema",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos del usuario a registrar",
                                    content = @Content(schema = @Schema(implementation = CreateUserDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación de datos",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "409", description = "Error de email o identificación existente",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/{identification}",
                    beanClass = Handler.class,
                    beanMethod = "getUserByIdentificationEntryPoint",
                    operation = @Operation(
                            operationId = "getUserbyIdentification",
                            summary = "Buscar un usuario por número de identificación",
                            description = "Busca un usuario por su número de identificación. Devuelve el usuario si lo encuentra, o una respuesta 200 OK con un cuerpo vacío si no.",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto correctamente o no encontrado",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class))),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                            content = @Content(mediaType = "application/json",
                                                    schema = @Schema(implementation = String.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler, TaskPath taskPath) {
        return route(POST(taskPath.getTasks()), handler::registerUserEntryPoint)
                .andRoute(GET(taskPath.getTasksById()), handler::getUserByIdentificationEntryPoint);
    }
}
