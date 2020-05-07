# BungeeProxy

BungeeProxy is a BungeeCord plugin that enables compatibility with HAProxy's PROXY v1 and v2 protocols.

**NB: Modern BungeeCord versions have PROXY protocol support built in with the `proxy_protocol` option for `listener` directives in `config.yml`**


## What's HAProxy?

[HAProxy](http://www.haproxy.org/) is a high performance, open-source Load Balancer and Reverse Proxy for TCP-based services.


## What's the PROXY protocol?

The PROXY protocol was designed to transmit a HAProxy client's connection information to the destination through other proxies or NAT routing (or both). HAProxy must be configured to send PROXY packets to your BungeeCord backend(s) in the `backend` or `server` configuration using the `send-proxy` or `send-proxy-v2` (v2 recommended) option.


## Installing HAProxy 1.5 / 1.6 on Ubuntu or Debian

HAProxy 1.5 or above is required to use the PROXY protocol.

Please see [https://haproxy.debian.net](https://haproxy.debian.net) for instructions on how to obtain a more recent package of HAProxy on Ubuntu or Debian.


## Example configuration

<pre>
listen minecraft
	bind :25565
	mode tcp
	balance leastconn
	option tcp-check
	server minecraft1 192.168.0.1:25565 check-send-proxy check send-proxy-v2
	server minecraft2 192.168.0.2:25565 check-send-proxy check send-proxy-v2
	server minecraft3 192.168.0.3:25565 check-send-proxy check send-proxy-v2
	server minecraft4 192.168.0.4:25565 check-send-proxy check send-proxy-v2
	server minecraft5 192.168.0.5:25565 check-send-proxy check send-proxy-v2
	server minecraft6 192.168.0.6:25565 check-send-proxy check send-proxy-v2
	server minecraft7 192.168.0.7:25565 check-send-proxy check send-proxy-v2
	server minecraft8 192.168.0.8:25565 check-send-proxy check send-proxy-v2
</pre>


## How do I know it's working?

In BungeeCord or Spigot (with IP Forwarding enabled), you should see the actual IP of players connecting through HAProxy. Don't forget to put BungeeProxy.jar in your BungeeCord's plugins folder.
