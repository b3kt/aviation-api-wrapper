package com.github.b3kt.aviation.application.executor;

import com.github.b3kt.aviation.application.command.Command;
import com.github.b3kt.aviation.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central command executor that routes commands to appropriate handlers.
 * Uses Spring's ApplicationContext for handler lookup and caches handlers for
 * performance.
 */
@Service
public class CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, CommandHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public CommandExecutor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Executes a command by finding and invoking the appropriate handler.
     * 
     * @param command the command to execute
     * @param <R>     the response type
     * @return Mono emitting the response
     */
    @SuppressWarnings("unchecked")
    public <R> Mono<R> execute(Command<R> command) {
        log.debug("Executing command: {}", command.getClass().getSimpleName());

        CommandHandler<Command<R>, R> handler = (CommandHandler<Command<R>, R>) findHandler(command);

        return handler.handle(command)
                .doOnSuccess(
                        result -> log.debug("Command executed successfully: {}", command.getClass().getSimpleName()))
                .doOnError(
                        error -> log.error("Command execution failed: {}", command.getClass().getSimpleName(), error));
    }

    /**
     * Finds the appropriate handler for a command.
     * Uses caching to avoid repeated Spring context lookups.
     */
    @SuppressWarnings("unchecked")
    private <R> CommandHandler<Command<R>, R> findHandler(Command<R> command) {
        Class<?> commandClass = command.getClass();

        return (CommandHandler<Command<R>, R>) handlerCache.computeIfAbsent(commandClass, clazz -> {
            Map<String, CommandHandler> handlers = applicationContext.getBeansOfType(CommandHandler.class);

            return handlers.values().stream()
                    .filter(handler -> handler.getCommandType().equals(commandClass))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No handler found for command: " + commandClass.getSimpleName()));
        });
    }
}
