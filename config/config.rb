# Serve HTTP traffic on this port
set  :bind, "192.168.86.55"
set  :port, 4567

riemann_base = '/var/lib/gems/1.9.1/gems/riemann-dash-0.2.8'
riemann_src = "#{riemann_base}/lib/riemann/dash"

# Add custom controllers in controller/
config.store[:controllers] = ["#{riemann_src}/controller"]

# Use the local view directory instead of the default
config.store[:views] = "#{riemann_src}/views"

# Specify a custom path to your workspace config.json
config.store[:ws_config] = "/vagrant/config/dash-config.json"

# Serve static files from this directory
config.store[:public] = "#{riemann_src}/public"
