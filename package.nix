{ pkgs }:
let
  buildMavenPackage = (pkgs.maven.override { inherit jdk; }).buildMavenPackage;
  jdk = pkgs.jdk17;
in
buildMavenPackage rec {
  pname = "missilewars";
  src = ./.;
  mvnHash = "sha256-Ihti60D5ag+D/yvmvp+CWkxHkSphfENiz28K01yWkPA=";
  version = "4.7.0";
  installPhase = ''
    mkdir -p $out/plugins
    cp missilewars-plugin/target/MissileWars-${version}.jar $out/plugins/${pname}-${version}.jar
  '';
  meta = with pkgs.lib; {
    description = "MissileWars is a famous, fun and fast minigame spigot-plugin for Minecraft";
    homepage = "https://github.com/RedstoneFuture/missilewars";
    license = licenses.gpl3;
  };
}
