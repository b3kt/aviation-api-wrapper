package com.github.b3kt.aviation.application.command;

import reactor.core.publisher.Mono;

/**
 * Generic interface for command handlers.
 * Each command handler is responsible for executing a specific command.
 * 
 * @param <C> the command type
 * @param <R> the response type
 */
public interface CommandHandler<C extends Command<R>, R> {

    /**
     * Handles the given command and returns a response.
     * 
     * @param command the command to handle
     * @return Mono emitting the response
     */
    Mono<R> handle(C command);

    /**
     * Returns the command type this handler supports.
     * Used by the CommandExecutor for routing.
     * 
     * @return the command class
     */
    Class<C> getCommandType();
}
